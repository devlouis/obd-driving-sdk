package com.mdp.innovation.obd_driving

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.util.Log
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.core.service.LocationUpdatesService
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.data.IoTHub.SendDataOBD
import kotlinx.android.synthetic.main.activity_main_test.*

class TestMainActivity : BaseAppCompat(), ObdGatewayVin {




    val TAG = javaClass.simpleName
    //GPS Service
    private var mLocationUpdatesService : LocationUpdatesService? = null
    private var isServiceBoundLocation: Boolean = false

    var sendata = SendDataOBD()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test)
        onClickListener()
        //sendata.InitClient()
        //Handler().post(mQueueCommands2)

    }

    var count = 0
    val mQueueCommands2 = object : Runnable {
        override fun run() {
            count++
            sendata.sendData2(count)
            Handler().postDelayed(this, 500)
        }
    }

    fun onClickListener(){

        button.setOnClickListener {
            if (!ConnectOBD.CheckConecction())
                ConnectOBD.startLiveData(this)
            else
                ConnectOBD.stopLiveData()

        }

        btnGoPair.setOnClickListener {
            nextActivity(PairObdActivity::class.java, true)
            //nextActivity(SensorActivity::class.java, true)
        }
        btnCheckConecction.setOnClickListener {
            ConnectOBD.CheckConecction()
        }

        btnVerifyMac.setOnClickListener {
            Log.v(TAG, " verifyMacOBD: ${ConnectOBD.verifyMacOBD()}")
            tviVerifyMac.text = "Boolean: ${ConnectOBD.verifyMacOBD().result} \nMac: ${ConnectOBD.verifyMacOBD().macBluetooth}"
        }


    }


    private fun showDialodAlert(msg: String){
        val builderAlertDialog = AlertDialog.Builder(this@TestMainActivity)
        builderAlertDialog.setTitle("Driving OBD")
        builderAlertDialog.setMessage(msg)
        builderAlertDialog.setPositiveButton("Ok") {dialog, which ->
            dialog.dismiss()
        }
        var dialog = builderAlertDialog.create()
        dialog.show()
    }

    override fun onDestroy() {
      /*  LogUtils().v("OBDRestar", "OBDRestar")
        ConnectOBD.restarServiceOBD()*/
        super.onDestroy()
    }

    /**
     * GPS
     */
    // Monitors the state of the connection to the service.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            // val binder = service as LocationUpdatesService.LocalBinder
            val binder = binder as LocationUpdatesService.LocalBinder
            //mLocationUpdatesService = (binder as LocationUpdatesService.LocalBinder).service
            mLocationUpdatesService = binder.service
            mLocationUpdatesService!!.requestLocationUpdates()

/*            val binder = service as LocationUpdatesService.LocalBinder
            mLocationUpdatesService = binder.service
            mLocationUpdatesService!!.requestLocationUpdates()*/

            isServiceBoundLocation = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mLocationUpdatesService = null
            isServiceBoundLocation = false
        }
    }

    override fun onStart() {
        super.onStart()
       /* bindService(
            Intent(this, LocationUpdatesService::class.java), mServiceConnection,
            Context.BIND_AUTO_CREATE
        )*/

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


    override fun getVin(vin: String) {
        Log.v(TAG, " VIN___: $vin")
        runOnUiThread {
            tviVIN.text = vin
            // ConnectOBD.stopLiveData()
        }
    }

    override fun errorConnect(message: String) {
        Log.v(TAG, " errorConnect: $message")
        runOnUiThread {
            showDialodAlert("${message}")
        }

    }

    override fun getSpeedKm(kmh: String) {
        Log.v(TAG, " getSpeedKm: $kmh")
    }


    /**
     * TEST CHECK BD
     */
/*
    private fun updateArticle(id: String) {
        realm.beginTransaction()

        val article = realm.where(TripDrivingEntity::class.java).equalTo("id", id).findFirst()
        *//* article.setTitle(title)
         article.setDescription(description)*//*


        realm.commitTransaction()

        LogUtils().v(TAG, " : ${article.toString()}")

    }*/


}
