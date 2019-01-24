package com.mdp.innovation.obd_driving.ui.fragment

import android.bluetooth.BluetoothAdapter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import com.mdp.innovation.obd_driving.ui.MyScoreView
import kotlinx.android.synthetic.main.fragment_my_score.*
import android.support.annotation.Nullable
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import com.mdp.innovation.obd_driving.interactor.MyScoreInteractor
import com.mdp.innovation.obd_driving.presenter.MyScorePresenter
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.mdp.innovation.obd_driving.service.model.MyScoreResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.util.*
import org.koin.android.ext.android.inject
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import org.jetbrains.anko.doAsync
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL


class MyScoreFragment : BaseServiceFragment(), MyScoreView, HomeActivity.StartLiveDataInterface {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): MyScoreFragment{
            return MyScoreFragment()
        }
        var appFirstStart = true
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

    private lateinit var myReceiver: MyReceiver


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
        myReceiver = MyReceiver()
        initUI()

        /*if(isActiveCollectDataService()){
            navigator.navigateToCollectData(fragmentManager, R.id.content)
            Global.cancelValidated = false
        }*/
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

/*    override fun errorConnect(message: String){
        Log.i("[INFO]","ACTIVITY errorConnect: $message")
        activity!!.runOnUiThread {
            hideLoading()
            Message.toastLong(message,activity!!.applicationContext)

        }
    }*/

    val OBD_LOST = 404
    val OBD_ERROR = 401
    val OBD_NO_PAIRED = 301
    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras
            val speed = extras.getString(ConnectOBD.EXTRA_SPEED)
            val typeError = extras.getInt(ConnectOBD.EXTRA_ERROR_TYPE)
            val messageError = extras.getString(ConnectOBD.EXTRA_ERROR_MSG)

           if (typeError != 0){
               hideLoading()
                when (typeError) {
                    /**
                     * Se dejo de recibir informacion de OBD
                     */
                    OBD_LOST -> {

                    }
                    OBD_ERROR -> {
                        LogUtils().v("CollDataFrag ", " errorConnect: $messageError")
                        //Message.toastLong(messageError,context)
                        showDialodAlert("${messageError}")
                    }
                    OBD_NO_PAIRED -> {
                        LogUtils().v("CollDataFrag ", " errorConnect: $messageError")
                        //Message.toastLong(messageError,context)
                        showDialodAlert("${messageError}")
                    }
                    else -> {
                        showDialodAlert("${messageError}")
                    }
                }
            }
        }
    }


    private fun showDialodAlert(msg: String) {
        val builderAlertDialog = AlertDialog.Builder(requireActivity())
        builderAlertDialog.setTitle("Driving OBD")
        builderAlertDialog.setMessage(msg)
        builderAlertDialog.setPositiveButton("Ok") { dialog, which ->
            dialog.dismiss()
        }
        var dialog = builderAlertDialog.create()
        dialog.show()
    }

    /*override fun onStop() {
        if (ConnectOBD.isServiceBoundLocation) {
            // Desconectarse del servicio.
            // Esto le indica al servicio que esta actividad ya no está en primer plano
            // y que el servicio puede responder promoviéndose a sí mismo a un servicio en primer plano.
            ConnectOBD.doUnbindServiceLocation()
        }
        super.onStop()
    }*/

    private fun initUI(){

        if(showScorePending) showScorePendingProgress()
        LogUtils().v("BD_LOCAL", " ${ConnectOBD.CheckConecction()}####################################")

    /*    if(appFirstStart){
            navigator.navigateToCollectData(fragmentManager, R.id.content)
        }*/

        if(ConnectOBD.CheckConecction()){
            navigator.navigateToCollectData(fragmentManager, R.id.content)
        }

        CustomAnimate.setButtonAnimation(btnStartTrip)

        btnStartTrip.setOnClickListener {
            //nextActivity(PairObdActivity::class.java, true)
            //navigator.navigateToCollectData(fragmentManager, R.id.content)

            showLoading()
            it.isEnabled = false
            it.postDelayed({

                Connection.validate(activity!!, object : Connection.OnConnectionFinishedListener{
                    override fun onConnectionFinished(code: Int) {
                        when(code){
                            Connection.OK ->{
                                hideLoading()
                                presenter.isConnected()
                                it.isEnabled = true
                            }
                            Connection.NO_NETWORK ->{
                                hideLoading()
                                Message.toastLong("Debe conectarse a una red.", context)
                                it.isEnabled = true
                            }
                            Connection.NO_CONNECTION ->{
                                hideLoading()
                                Message.toastLong("Hay problemas con su señal de internet. Inténtelo nuevamente.", context)
                                it.isEnabled = true
                            }
                            Connection.EXCEPTION ->{
                                hideLoading()
                                Message.toastLong("Ocurrió un problema. Inténtelo en unos minutos.", context)
                                it.isEnabled = true
                            }
                        }
                    }
                })


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

        if(context == null) return

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
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            myReceiver, IntentFilter(ConnectOBD.ACTION_BROADCAST)
        )
        (activity as HomeActivity).setOnStartLiveDataListener(this)
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(myReceiver)
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
        //navigator.navigateToCollectData(fragmentManager, R.id.content)




        //DEMO
        /*startCollectDataService()
        (activity as HomeActivity).simulateSpeed()
        Global.cancelValidated = false*/
        //navigator.navigateToCollectData(fragmentManager, R.id.content)
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
