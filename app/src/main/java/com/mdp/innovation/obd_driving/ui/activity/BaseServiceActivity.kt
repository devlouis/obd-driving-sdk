package com.mdp.innovation.obd_driving.ui.activity

import android.app.ActivityManager
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mdp.innovation.obd_driving.util.Message
import android.os.Build
import android.os.Bundle
import com.mdp.innovation.obd_driving.internal.CollectTripDataService
import com.mdp.innovation.obd_driving.ui.fragment.EndTripDialogFragment
import com.mdp.innovation.obd_driving.util.Constants
import android.app.Activity
import com.mdp.innovation.obd_driving.util.Preferences
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import org.koin.android.ext.android.inject


open class BaseServiceActivity : BaseAppCompat() {

    var collectDataService = CollectTripDataService::class.java
    lateinit var collectDataIntent : Intent

    private val preferences by inject<Preferences>()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        collectDataIntent = Intent(applicationContext, collectDataService)
    }

    fun startCollectDataService(){
        collectDataIntent.action = Constants.STARTFOREGROUND_ACTION
        startService(collectDataService, collectDataIntent)
    }
    fun stopCollectDataService(){
        collectDataIntent.action = Constants.STOPFOREGROUND_ACTION
        stopService(collectDataService, collectDataIntent)

        //ConnectOBD.stopLiveData()

        preferences.setScorePending(applicationContext, true)

        val returnIntent = Intent()
        returnIntent.putExtra("result", "end_trip")
        setResult(Activity.RESULT_OK, returnIntent)

        this.onBackPressed()
    }
    fun isActiveCollectDataService() : Boolean{
        return serviceIsActive(collectDataService)
    }

    fun startService(serviceClass: Class<*>, intent: Intent){
        if (!isServiceRunning(serviceClass)) {
            //startService(intent)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }

        } else {
            Log.i("[INFO]", "Service already running.")
            Message.toastShort("Service already running.", applicationContext)
        }
    }

    fun stopService(serviceClass: Class<*>, intent: Intent){
        if (isServiceRunning(serviceClass)) {
            stopService(intent)
        } else {
            Log.i("[INFO]", "Service already stopped.")
            Message.toastShort("Service already stopped.", applicationContext)
        }
    }

    fun serviceIsActive(serviceClass: Class<*>): Boolean{
        if (isServiceRunning(serviceClass)) {
            Log.i("[INFO]", "Service is running.")
            Message.toastShort("Service is running.", applicationContext)
            return true
        } else {
            Log.i("[INFO]", "Service is stopped.")
            Message.toastShort("Service is stopped.", applicationContext)
            return false
        }
    }

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

}
