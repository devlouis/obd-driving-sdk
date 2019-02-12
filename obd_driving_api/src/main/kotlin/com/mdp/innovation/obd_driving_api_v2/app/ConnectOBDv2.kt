package com.mdp.innovation.obd_driving_api_v2.app

import android.app.Application
import android.content.*
import android.os.IBinder
import android.widget.Toast
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference
import com.mdp.innovation.obd_driving_api.data.store.TripRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.FailuresTripValuesRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.LocationRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.ObdRepository
import com.mdp.innovation.obd_driving_api_v2.constants.DefineObdReader.*
import com.mdp.innovation.obd_driving_api_v2.service.LocationIntentService
import com.mdp.innovation.obd_driving_api_v2.service.ObdReaderService
import com.mdp.innovation.obd_driving_api_v2.trip.TripRecord


object ConnectOBDv2{

    val TAG = javaClass.simpleName
    val TAG_BD = " BD_LOCAL_V2"
    val TAG_GET = " TAG_GET"

    lateinit var mContext: Context

    lateinit var appSharedPreference: SharedPreference

    private var macDevice = ""
    var VIN = ""
    var mLocationUpdatesService = LocationIntentService()
    var mObdReaderService: ObdReaderService? = null

    var isServiceBoundOBD = false
    var isServiceBoundLocation = false


    init {
        LogUtils().v(TAG_BD, " Singleton class invoked. ")
    }

    fun initialize(context: Context) {
        mContext = context
        LogUtils().v(TAG_BD," mContext: ${mContext.getString(R.string.status_bluetooth_ok)}")

        appSharedPreference = SharedPreference(context)
        macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        VIN =  appSharedPreference.getVinCar()[appSharedPreference.VIN_CAR]!!

        LogUtils().v(TAG_BD, " macDevice:: $macDevice")

        //startLiveData()

    }

    private var serviceIntent = Intent()
    private var serviceIntentLocation = Intent()
    fun startLiveData(){
        isServiceBoundOBD = true
        /**
         * Register receiver with some action related to OBD connection status
         */
        serviceIntent = Intent(mContext.applicationContext, ObdReaderService::class.java)
        mContext.startService(serviceIntent)
        serviceIntentLocation = Intent(mContext.applicationContext, LocationIntentService::class.java)


        /**
         * Limpiar BDs
         */
        TripRepository(Application()).deleteAll()
        ObdRepository(Application()).deleteAll()
        LocationRepository(Application()).deleteAll()
        FailuresTripValuesRepository(Application()).deleteAll()

    }

    fun stopLiveData(){
        ObdReaderService.shouldContinue = false
        isServiceBoundOBD = false
        LocationIntentService().shouldContinueLoca = false
        //mContext.stopService(Intent(mContext.applicationContext, ObdReaderService::class.java))
        //mContext.stopService(Intent(mContext.applicationContext, LocationIntentService::class.java))
    }


    fun CheckConecction(): Boolean{
        return isServiceBoundOBD
    }




}
