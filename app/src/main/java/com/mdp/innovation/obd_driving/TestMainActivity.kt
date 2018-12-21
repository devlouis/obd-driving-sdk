package com.mdp.innovation.obd_driving

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.core.service.LocationUpdatesService
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import com.mdp.innovation.obd_driving_api.app.utils.JSONUtils
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.data.IoTHub.SendDataOBD
import com.mdp.innovation.obd_driving_api.data.entity.ObdEntity
import com.mdp.innovation.obd_driving_api.data.entity.TripDrivingEntity
import com.mdp.innovation.obd_driving_api.data.entity.TripEntity
import com.mdp.innovation.obd_driving_api.data.store.TripRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.LocationRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.ObdRepository
import kotlinx.android.synthetic.main.activity_main_test.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel

class TestMainActivity : BaseAppCompat(), ObdGatewayVin {




    val TAG = javaClass.simpleName
    //GPS Service
    private var mLocationUpdatesService : LocationUpdatesService? = null
    private var isServiceBoundLocation: Boolean = false

    var sendata = SendDataOBD()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test)

        tripRepository = (application as MyApplication).tripRepository
        obdRepository = (application as MyApplication).obdRepository
        locationRepository = (application as MyApplication).locationRepository

        onClickListener()
        sendata.InitClient()
        //Handler().post(mQueueCommands2)

        updateArticle("6")

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

        idSendDataIoT.setOnClickListener {
            getFirstTrip(10)
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

    private var tripRepository: TripRepository? = null
    private var obdRepository: ObdRepository? = null
    private var locationRepository: LocationRepository? = null


    private fun updateArticle(id: String) {

        tripRepository!!.getAllNotes(object : TripRepository.PopulateCallback {
            override fun onSuccess(tripEntityList: List<TripEntity>) {
                //renderNotes(noteEntities)
                LogUtils().v(TAG, " id viaje : ${tripEntityList.toString()}")
                //tripRepository!!.deleteAll()

            }

            override fun onFailure(e: Exception) {
                //showErrorMessage(e.toString())
                LogUtils().v(TAG, " id viaje : error ${e.toString()}")
            }
        })
        //

    }

    fun getFirstTrip(limit: Int){
        exportDB()

/*

        tripRepository!!.getFirtsTrips(10, object : TripRepository.PopulateCallback {
            override fun onSuccess(tripEntityList: MutableList<TripEntity>?) {
                //LogUtils().v(TAG, " LOS PRIMEROS 10 : ${tripEntityList.toString()}")
                LogUtils().v(TAG, " LOS PRIMEROS 10 gson : ${JSONUtils.generateJSONArray(tripEntityList).toString()}")
                sendata.sendDataJsonString(JSONUtils.generateJSONArray(tripEntityList).toString())
                //exportDB()
            }

            override fun onFailure(e: java.lang.Exception?) {
                LogUtils().v(TAG, " getFirstTrip: error ${e.toString()}")
            }
        })

        obdRepository!!.getFirtsTrips(10, object : ObdRepository.PopulateCallback {
            override fun onSuccess(obdEntityList: MutableList<ObdEntity>) {
                //LogUtils().v(TAG, " LOS PRIMEROS 10 : ${obdEntityList.toString()}")
                LogUtils().v(TAG, " LOS PRIMEROS 10 gson : ${JSONUtils.generateJSONArray(obdEntityList).toString()}")
                sendata.sendDataJsonString(JSONUtils.generateJSONArray(obdEntityList).toString())
                //exportDB()
            }
            override fun onFailure(e: java.lang.Exception?) {
                LogUtils().v(TAG, " getFirstTrip: error ${e.toString()}")
            }
        })
*/



    }

    val SAMPLE_DB_NAME = "BDRoomTrip"
    private fun exportDB() {
        val sd = Environment.getExternalStorageDirectory()
        val data = Environment.getDataDirectory()
        var source: FileChannel? = null
        var destination: FileChannel? = null
        val currentDBPath = "/data/com.mdp.innovation.obd_driving/databases/$SAMPLE_DB_NAME"
        val backupDBPath = "$SAMPLE_DB_NAME.db"
        val currentDB = File(data, currentDBPath)
        val backupDB = File(sd, backupDBPath)
        try {
            source = FileInputStream(currentDB).channel
            destination = FileOutputStream(backupDB).channel
            destination!!.transferFrom(source, 0, source!!.size())
            source.close()
            destination.close()
            Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show()
            //tripRepository!!.deleteAll()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }



}
