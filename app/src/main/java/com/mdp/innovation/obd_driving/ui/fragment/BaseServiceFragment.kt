package com.mdp.innovation.obd_driving.ui.fragment

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.util.Log

import com.mdp.innovation.obd_driving.internal.CollectTripDataService
import com.mdp.innovation.obd_driving.util.Constants
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving.util.Message
import com.mdp.innovation.obd_driving.util.Preferences
import org.koin.android.ext.android.inject

open class BaseServiceFragment : BaseFragment() {

    var collectDataService = CollectTripDataService::class.java
    lateinit var collectDataIntent : Intent

    private val preferences by inject<Preferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectDataIntent = Intent(context, collectDataService)
    }

    fun startCollectDataService(){
        collectDataIntent.action = Constants.STARTFOREGROUND_ACTION
        collectDataIntent.putExtra("onStop", object : ResultReceiver(null) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                Log.d("Sabee", "overideeeeeeeeeeeeeeeeee onStopService")
                Global.tripIsEnded = true
                fragmentManager?.popBackStack()
            }
        })
        startService(collectDataService, collectDataIntent)
    }
    fun stopCollectDataService(){
        collectDataIntent.action = Constants.STOPFOREGROUND_ACTION
        stopService(collectDataService, collectDataIntent)

        //ConnectOBD.stopLiveData()

        preferences.setScorePending(context, true)

    }
    fun isActiveCollectDataService() : Boolean{
        return serviceIsActive(collectDataService)
    }

    private fun startService(serviceClass: Class<*>, intent: Intent){
        if (!isServiceRunning(serviceClass)) {
            //startService(intent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activity?.startForegroundService(intent)
            } else {
                activity?.startService(intent)
            }

        } else {
            Log.i("[INFO]", "Service already running.")
            //Message.toastShort("Service already running.", context)
        }
    }

    private fun stopService(serviceClass: Class<*>, intent: Intent){
        if (isServiceRunning(serviceClass)) {
            activity?.stopService(intent)
        } else {
            Log.i("[INFO]", "Service already stopped.")
            //Message.toastShort("Service already stopped.", context)
        }
    }

    private fun serviceIsActive(serviceClass: Class<*>): Boolean{
        if (isServiceRunning(serviceClass)) {
            Log.i("[INFO]", "Service is running.")
            //Message.toastShort("Service is running.", context)
            return true
        } else {
            Log.i("[INFO]", "Service is stopped.")
            //Message.toastShort("Service is stopped.", context)
            return false
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = activity?.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
