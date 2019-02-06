package com.mdp.innovation.obd_driving.ui.fragment

import android.app.Activity
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
import com.google.android.gms.common.api.Status
import com.google.firebase.iid.FirebaseInstanceId
import com.mdp.innovation.obd_driving.interactor.TokenInteractor
import com.mdp.innovation.obd_driving.service.model.MyScoreResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.model.UpdateTokenResponse
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

    private var showScorePending = false

    private var VIN = ""
    private var tripId = ""
    private var userId = ""

    private lateinit var myReceiver: MyReceiver
    private lateinit var myReceiverPush: MyReceiverPush

    private val START_TRIP = 1
    private val REFRESH_SCORE = 2


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
        myReceiverPush = MyReceiverPush()
        initUI()

        /*if(isActiveCollectDataService()){
            navigator.navigateToCollectData(fragmentManager, R.id.content)
            Global.cancelValidated = false
        }*/
    }

    fun getVin(vin: String){
        Log.i("[INFO]","ACTIVITY getVin: $vin")
        //activity!!.runOnUiThread {
            hideLoading()

            Global.myVIN = vin
            preferences.setVIN(context,vin)
            //ConnectOBD.stopLiveData()
            navigator.navigateToCollectData(fragmentManager, R.id.content)
            Global.cancelValidated = false
        //}

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
            val vin = extras.getString(ConnectOBD.EXTRA_VIN)
            val speed = extras.getString(ConnectOBD.EXTRA_SPEED)
            val typeError = extras.getInt(ConnectOBD.EXTRA_ERROR_TYPE)
            val messageError = extras.getString(ConnectOBD.EXTRA_ERROR_MSG)

            if (vin.isNotEmpty()){
                getVin(vin)
            }else if (typeError != 0){
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

    private inner class MyReceiverPush : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras
            val type = extras.getString(Constants.ACTION_TYPE_PUSH)

            when(type){
                Constants.TYPE_TRIP_FINISHED -> {
                    showMyScore()
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
            return
        }

        /*var tokenPush = preferences.getTokenPush(context)
        if(tokenPush == "-"){
            tokenPush = FirebaseInstanceId.getInstance().token!!
            preferences.setTokenPush(context, tokenPush)
            //ENDPOINT ADD TOKEN PUSH

            val dataUser = preferences.getDataUser(context)

            val interactor = TokenInteractor()
            interactor.updateToken(object: TokenInteractor.OnUpdateTokenFinishedListener{
                override fun onUpdateTokenSuccess(response: UpdateTokenResponse) {
                    Log.d(TAG,"El token se actualizó correctamente.")
                }

                override fun onUpdateTokenError(message: String) {
                    Log.d(TAG,"Error: $message")
                }
            }, dataUser!!.userId!!, tokenPush, "-")

        }*/

        CustomAnimate.setButtonAnimation(btnStartTrip)

        btnStartTrip.setOnClickListener {

            /*val token = preferences.getTokenPush(context)
            Log.d(TAG, token)*/

            showLoading()
            it.postDelayed({

                validateBluetooth(it)

                /*val data = preferences.getDataUser(context)
                Log.d(TAG, data!!.connectionString!!)*/

            }, 100L)

        }

        //showProgress()

        showMyScore()

        userId = preferences.getDataUser(context)!!.userId!!

        img_refresh.setOnClickListener {
            Log.d(TAG, "clickkkkkkkkk")

            it.isEnabled = false
            val startRotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate_spin2)
            it.startAnimation(startRotateAnimation)
            validateInternet(REFRESH_SCORE)

        }

    }

    private fun showMyScore(){
        var myScore = preferences.getMyScore(context)
        if(myScore == null || myScore == "null") myScore = "-"
        if(myScore.endsWith(".0")) myScore = myScore.replace(".0","")
        tv_home_prom.text = myScore
    }

    override fun onGetMyScoreSuccess(response: MyScoreResponse) {

        if(context == null || img_refresh == null) return

        refreshButtonRestored()

        if(response != null){

            var scoreStr = "-"

            if(response.score != null){
                try {
                    scoreStr = response.score.toString()
                }catch (ex: Exception){}
            }

            preferences.setMyScore(context, scoreStr)
            showMyScore()

        }
    }

    override fun onGetMyScoreError(message: String) {
        refreshButtonRestored()
        Message.toastLong("Ha ocurrido un error: $message", context)
    }

    private fun refreshButtonRestored(){
        img_refresh.clearAnimation()
        img_refresh.isEnabled = true
    }

    override fun onResume() {
        super.onResume()

        showMyScore()

        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            myReceiver, IntentFilter(ConnectOBD.ACTION_BROADCAST)
        )
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            myReceiverPush, IntentFilter(Constants.ACTION_BROADCAST_PUSH)
        )
        (activity as HomeActivity).setOnStartLiveDataListener(this)
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(myReceiver)
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(myReceiverPush)
        super.onPause()
        (activity as HomeActivity).setOnStartLiveDataListener(null)
    }

    override fun onDeviceConnected(){
        goToStartTrip()
    }

    override fun onDeviceNoConnected(){
        (activity as HomeActivity).goToPairObd()
        Message.toastLong("Primero debe conectarse a su OBD.", activity?.applicationContext)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == GPS.REQUEST_LOCATION) {
            if(resultCode == Activity.RESULT_OK){
                //Message.toastLong("El GPS está activo ahora!!!!.", context)
                validateInternet(START_TRIP)
            }else if (resultCode == Activity.RESULT_CANCELED) {
                hideLoading()
                Message.toastLong("Debe activar el GPS para continuar.", context)
            }
        }
    }

    private fun validateBluetooth(v: View){
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            hideLoading()
            Message.toastLong("Lo sentimos. Su dispositivo no cuenta con Bluetooth.", activity?.applicationContext)
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                mBluetoothAdapter.enable()
            }
            validateGPS(v)
        }
    }

    private fun validateGPS(it: View){
        GPS.validate(activity!!, object : GPS.OnConnectionFinishedListener{
            override fun onGPSConnection(code: Int) {
                when(code){
                    GPS.OK ->{
                        //hideLoading()
                        //Message.toastLong("El GPS está activo.", context)
                        validateInternet(START_TRIP)
                    }
                    GPS.NO_ENABLED ->{
                        //hideLoading()
                        Message.toastLong("El GPS está apagado.", context)
                    }
                    GPS.NO_SUPPORTED ->{
                        hideLoading()
                        Message.toastLong("Su equipo no cuenta con GPS.", context)
                    }
                    GPS.ERROR ->{
                        hideLoading()
                        Message.toastLong("Ocurrió un problema. Inténtelo en unos minutos.", context)
                    }
                }
            }

            override fun onStartResolution(status: Status) {
                startIntentSenderForResult(status.resolution.intentSender, GPS.REQUEST_LOCATION, null, 0, 0, 0, null)
            }
        })
    }

    private fun validateInternet(action: Int){
        Connection.validate(activity!!, object : Connection.OnConnectionFinishedListener{
            override fun onConnectionFinished(code: Int) {
                when(code){
                    Connection.OK ->{
                        when(action){
                            START_TRIP->{
                                //hideLoading()
                                presenter.isConnected()
                            }
                            REFRESH_SCORE->{
                                presenter.getMyScore(userId)
                            }
                        }
                    }
                    Connection.NO_NETWORK ->{
                        when(action){
                            START_TRIP-> hideLoading()
                            REFRESH_SCORE-> refreshButtonRestored()
                        }
                        Message.toastLong(resources.getString(R.string.no_network), context)
                    }
                    Connection.NO_CONNECTION ->{
                        when(action){
                            START_TRIP-> hideLoading()
                            REFRESH_SCORE-> refreshButtonRestored()
                        }
                        Message.toastLong(resources.getString(R.string.no_connection), context)
                    }
                    Connection.EXCEPTION ->{
                        when(action){
                            START_TRIP-> hideLoading()
                            REFRESH_SCORE-> refreshButtonRestored()
                        }
                        Message.toastLong(resources.getString(R.string.connection_exception), context)
                    }
                }
            }
        })
    }

    override fun showLoading(){
        loading.visibility = View.VISIBLE
    }
    override fun hideLoading(){
        loading.visibility = View.GONE
    }

    private fun goToStartTrip(){

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

    override fun onDestroyView() {
        presenter.onDestroy()
        super.onDestroyView()
    }

    private fun showScorePendingProgress(){
        pb_update_score.visibility = View.VISIBLE
    }

    private fun hideScorePendingProgress(){
        pb_update_score.visibility = View.GONE
    }
}
