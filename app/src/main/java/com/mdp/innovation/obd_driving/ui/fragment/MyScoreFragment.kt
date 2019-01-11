package com.mdp.innovation.obd_driving.ui.fragment

import android.bluetooth.BluetoothAdapter
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
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.mdp.innovation.obd_driving.service.model.MyScoreResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import org.koin.android.ext.android.inject
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving.util.Preferences
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import java.lang.Exception


class MyScoreFragment : BaseServiceFragment(), MyScoreView, HomeActivity.StartLiveDataInterface {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): MyScoreFragment{
            return MyScoreFragment()
        }
    }

    private val navigator by inject<Navigator>()
    private val preferences by inject<Preferences>()

    private val presenter = MyScorePresenter(this, MyScoreInteractor())

    private var runnable = Runnable {  }
    private var handler = Handler(Looper.getMainLooper())

    private var showScorePending = false

    private var VIN = ""
    private var tripId = ""
    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //validateConsultScore(true)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_my_score, container, false)

        //var drawerLayout = activity?.findViewById(R.id.drawer_layout) as DrawerLayout

        var toolbar = view.findViewById(R.id.includeToolbar) as Toolbar

        toolbar.title = "Mi Calificación"
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        drawerConfig(activity, toolbar)

        //validateConsultScore(true)

        if(Global.tripIsEnded){
            Message.toastLong(resources.getString(R.string.alert_end_trip), context)
            Global.tripIsEnded = false
        }

        return view
    }



    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()

        if(isActiveCollectDataService()){
            navigator.navigateToCollectData(fragmentManager, R.id.content)
            Global.cancelValidated = false
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
                    handler.postDelayed(runnable, 5000)
                }else{
                    handler.removeCallbacks(runnable)
                    showScorePending = false
                    if(!isFirstTime) {
                        isFirstTime = false
                        hideScorePendingProgress()
                    }
                }

            }
            handler.postDelayed(runnable, 5000)
        }else{
            showScorePending = false
            if(!isFirstTime) {
                isFirstTime = false
                hideScorePendingProgress()
            }
        }

    }

    private fun consultScore(){

        VIN = "D12"
        tripId = "0caacd33-1626-4767-93d1-5cbd3ce0217a"
        presenter.getScore(VIN, tripId)

        /*doAsync{
            //val service = WSService()
            val VIN = "D1"
            val tripId = "04a76229-b13b-427b-a1b9-695ea44a0ee2"
            //var request = ScoreRequest(VIN, tripId)
            //val response = service.getScore(request)



            uiThread{

                val acelerationScore = response.VIN?.aceleracion
                val brakingScore = response?.VIN?.frenado
                val newScore = 10 + (acelerationScore!!.toFloat()) + (brakingScore!!.toFloat())
                if(response != null && response?.VIN != null && acelerationScore != null && brakingScore != null){
                    val lastTripCalculed = preferences.getLastTripCalculed(context)
                    if(tripId != lastTripCalculed){
                        preferences.setLastTripCalculed(context, tripId)
                        preferences.setMyScore(context, newScore.toString())
                        preferences.setScorePending(context, false)
                        tv_home_prom.text = newScore.toString()
                        hideScorePendingProgress()

                        var dialog = EndTripDialogFragment()
                        dialog.show(fragmentManager,"end_trip")
                    }
                }

                //Toast.makeText(context,"El puerto es: " + newScore,Toast.LENGTH_LONG).show()
            }
        }*/
    }

    override fun onGetScoreSuccess(response: ScoreResponse) {
        if(response != null && response.VIN != null && response.VIN.aceleracion != null && response.VIN.frenado != null){
            val accelerationScore = response.VIN.aceleracion
            val brakingScore = response.VIN.frenado
            val penality = (accelerationScore.toFloat()) + (brakingScore.toFloat())
            val newScore = 10 + penality
            val lastTripCalculed = preferences.getLastTripCalculed(context)
            if(tripId != lastTripCalculed){
                preferences.setLastTripCalculed(context, tripId)
                preferences.setMyScore(context, newScore.toString())
                preferences.setScorePending(context, false)
                tv_home_prom.text = newScore.toString()
                hideScorePendingProgress()

                var dialog = EndTripDialogFragment()
                dialog.show(fragmentManager,"end_trip")
            }
        }
    }

    override fun onGetScoreError(message: String) {
        Message.toastLong("Ha ocurrido un error: $message", context)
    }



    override fun getVin(vin: String){
        Log.i("[INFO]","ACTIVITY getVin: $vin")
        activity!!.runOnUiThread {
            hideLoading()

            Global.myVIN = vin
            preferences.setVIN(context,vin)
            //ConnectOBD.stopLiveData()
            navigator.navigateToCollectData(fragmentManager, R.id.content)
            Global.cancelValidated = false
        }

    }

    override fun getSpeedKm(kmh: String) {

    }

    override fun errorConnect(message: String){
        //hideProgress()
        //Message.toastLong(message,context)

        Log.i("[INFO]","ACTIVITY errorConnect: $message")
        activity!!.runOnUiThread {
            hideLoading()
            Message.toastLong(message,activity!!.applicationContext)
            navigator.navigateToCollectData(fragmentManager, R.id.content)

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

        var myScore = preferences.getMyScore(context)
        if(myScore == "null") myScore = "-"
        tv_home_prom.text = myScore

        userId = preferences.getDataUser(context)!!.userId!!

        img_refresh.setOnClickListener {
            Log.d(TAG, "clickkkkkkkkk")

            val startRotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_spin2)
            it.startAnimation(startRotateAnimation)

            presenter.getMyScore(userId)

            it.isEnabled = false

        }

    }

    override fun onGetMyScoreSuccess(response: MyScoreResponse) {
        img_refresh.clearAnimation()
        img_refresh.isEnabled = true
        if(response != null){

            var scoreStr = "-"

            if(response.score != null){
                try {
                    scoreStr = response.score.toString()
                }catch (ex: Exception){}
            }

            preferences.setMyScore(context, scoreStr)
            tv_home_prom.text = scoreStr

        }
    }

    override fun onGetMyScoreError(message: String) {
        img_refresh.clearAnimation()
        img_refresh.isEnabled = true
        Message.toastLong("Ha ocurrido un error: $message", context)
    }

    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).setOnStartLiveDataListener(this)
    }

    override fun onPause() {
        super.onPause()
        (activity as HomeActivity).setOnStartLiveDataListener(null)
    }

    override fun onDeviceConnected(){
        //nextActivity(HomeActivity::class.java, false)
        //Message.toastShort("Preparando para iniciar el viaje.", activity?.applicationContext)

        /*val manager = (activity?.getSystemService( Context.LOCATION_SERVICE ) as LocationManager)

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            Log.d("GPSx", "NOOOOOOOO")
        }else{
            Log.d("GPSx", "SIIIIIIIII")
        }*/


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

    override fun showLoading(){
        loading.visibility = View.VISIBLE
    }
    override fun hideLoading(){
        loading.visibility = View.GONE
    }

    private fun goToStartTrip(){

        handler.removeCallbacks(runnable)

        //SDK
        showLoading()
        (activity as HomeActivity).startLiveData()
        //(activity as HomeActivity).simulateSpeed()
        navigator.navigateToCollectData(fragmentManager, R.id.content)




        //DEMO
        /*startCollectDataService()
        (activity as HomeActivity).simulateSpeed()
        Global.cancelValidated = false
        navigator.navigateToCollectData(fragmentManager, R.id.content)*/
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    private fun showScorePendingProgress(){
        pb_update_score.visibility = View.VISIBLE
    }

    private fun hideScorePendingProgress(){
        pb_update_score.visibility = View.GONE
    }
}
