package com.mdp.innovation.obd_driving.ui.fragment

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import com.mdp.innovation.obd_driving.ui.MyScoreView
import com.mdp.innovation.obd_driving.util.Message
import kotlinx.android.synthetic.main.fragment_my_score.*
import android.support.annotation.Nullable
import com.mdp.innovation.obd_driving.interactor.MyScoreInteractor
import com.mdp.innovation.obd_driving.presenter.MyScorePresenter
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.CustomAnimate
import android.util.Log
import android.widget.Toast
import com.mdp.innovation.obd_driving.model.ScoreRequest
import com.mdp.innovation.obd_driving.service.WSService
import org.koin.android.ext.android.inject
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving.util.Preferences
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


class MyScoreFragment : BaseFragment(), MyScoreView, ObdGatewayVin {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): MyScoreFragment{
            return MyScoreFragment()
        }
    }

    var myActivity = HomeActivity()

    private val navigator by inject<Navigator>()
    private val preferences by inject<Preferences>()

    private val presenter = MyScorePresenter(this, MyScoreInteractor())

    var runnable = Runnable {  }
    var handler = Handler(Looper.getMainLooper())

    var showScorePending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        validateConsultScore(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_my_score, container, false)

        //var drawerLayout = activity?.findViewById(R.id.drawer_layout) as DrawerLayout

        var toolbar = view.findViewById(R.id.includeToolbar) as Toolbar

        toolbar.title = "Mi Calificación"
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        myActivity = activity as HomeActivity
        myActivity.drawerConfig(toolbar)

        /*toolbar.setNavigationOnClickListener {
            System.out.println("Menuuuuuuuu")
            drawerLayout.openDrawer(Gravity.START)
        }*/

        return view
    }



    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myActivity = activity as HomeActivity
        initUI()

        if(myActivity.isActiveCollectDataService()){
            navigator.navigateToCollectTripData(this, 1)
            Global.cancelValidated = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK){
            val result = data?.getStringExtra("result")
            if(result.equals("end_trip")){

                val handler = Handler()
                handler.postDelayed({

                    Message.toastLong(resources.getString(R.string.alert_end_trip), context)

                }, 500)

                validateConsultScore(false)

            }
        }
    }

    private fun validateConsultScore(isFirstTimeInput: Boolean){

        var isFirstTime = isFirstTimeInput

        if(preferences.getScorePending(context)){
            showScorePending = true
            if(!isFirstTime){
                isFirstTime = false
                showScorePendingProgress()
            }
            //consultScore()


            runnable = Runnable {

                if(preferences.getScorePending(context)){
                    consultScore()
                    handler.postDelayed(runnable, 10000)
                }else{
                    handler.removeCallbacks(runnable)
                    showScorePending = false
                    if(!isFirstTime) {
                        isFirstTime = false
                        hideScorePendingProgress()
                    }
                }

            }
            handler.postDelayed(runnable, 10000)
        }else{
            showScorePending = false
            if(!isFirstTime) {
                isFirstTime = false
                hideScorePendingProgress()
            }
        }

    }

    private fun consultScore(){
        doAsync{
            val service = WSService()
            var request = ScoreRequest("peter@klaven", "cityslicka")
            val response = service.getScore(request)

            uiThread{

                val scoreDate = response?.token
                val newScore = "9.5"
                if(scoreDate != null && scoreDate.trim() != ""){
                    val lastScore = preferences.getLastScoreDate(context)
                    if(scoreDate != lastScore){
                        preferences.setLastScoreDate(context, scoreDate)
                        preferences.setMyScore(context, newScore)
                        preferences.setScorePending(context, false)
                        tv_home_prom.text = newScore
                        hideScorePendingProgress()

                        var dialog = EndTripDialogFragment()
                        dialog.show(fragmentManager,"end_trip")
                    }
                }

                //Toast.makeText(context,"El puerto es: " + newScore,Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun getVin(vin: String){
        Log.i("[INFO]","ACTIVITY getVin: $vin")
        activity!!.runOnUiThread {
            hideProgress()

            Global.myVIN = vin
            preferences.setVIN(context,vin)
            //ConnectOBD.stopLiveData()
            navigator.navigateToCollectTripData(this, 1)
            Global.cancelValidated = false
        }

    }

    override fun getSpeedKm(kmh: String) {

    }

    override fun errorConnect(message: String){
        hideProgress()
        Message.toastLong(message,context)

        Log.i("[INFO]","ACTIVITY errorConnect: $message")
        activity!!.runOnUiThread {
            hideProgress()
            Message.toastLong(message,context)
        }
    }

    override fun onStop() {
        if (ConnectOBD.isServiceBoundLocation) {
            // Desconectarse del servicio.
            // Esto le indica al servicio que esta actividad ya no está en primer plano
            // y que el servicio puede responder promoviéndose a sí mismo a un servicio en primer plano.
            ConnectOBD.doUnbindServiceLocation()
        }
        super.onStop()
    }

    private fun initUI(){

        if(showScorePending) showScorePendingProgress()

        CustomAnimate.setButtonAnimation(btnStartTrip)

        btnStartTrip.setOnClickListener {
            //nextActivity(PairObdActivity::class.java, true)
            //navigator.navigateToCollectData(fragmentManager, R.id.content)

            it.isEnabled = false
            it.postDelayed({
                presenter.isConnected()
                it.isEnabled = true
            }, 100L)
        }

        //showProgress()

        val myScore = preferences.getMyScore(context)
        tv_home_prom.text = myScore

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDeviceConnected(){
        //nextActivity(HomeActivity::class.java, false)
        //Message.toastShort("Preparando para iniciar el viaje.", activity?.applicationContext)

        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Message.toastLong("Lo sentimos. Su dispositivo no cuenta con Bluetooth.", activity?.applicationContext)
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                // Bluetooth is not enable :)
                //Message.toastShort("Debe encender su Bluetooth.", activity?.applicationContext)
                mBluetoothAdapter.enable()
            }
            goToStartTrip()
        }



    }

    override fun onDeviceNoConnected(){
        (activity as HomeActivity).goToPairObd()
        Message.toastShort("Su OBD ha perdido la conexión. Por favor vuélvalo a conectar.", activity?.applicationContext)
    }

    override fun showProgress(){
        loading.visibility = View.VISIBLE
    }
    override fun hideProgress(){
        loading.visibility = View.GONE
    }

    private fun goToStartTrip(){

        handler.removeCallbacks(runnable)

        //SDK
        showProgress()
        ConnectOBD.startLiveData(this)

        //DEMO
        //myActivity.startCollectDataService()
        Global.cancelValidated = false
        //navigator.navigateToCollectTripData(this, 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    fun showScorePendingProgress(){
        pb_update_score.visibility = View.VISIBLE
    }

    fun hideScorePendingProgress(){
        pb_update_score.visibility = View.GONE
    }
}
