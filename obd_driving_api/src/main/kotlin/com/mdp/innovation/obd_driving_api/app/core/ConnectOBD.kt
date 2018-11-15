package com.mdp.innovation.obd_driving_api.app.core

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.ui.config.ObdConfig
import com.mdp.innovation.obd_driving_api.app.ui.io.*
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.commands.SpeedCommand
import com.mdp.innovation.obd_driving_api.commands.control.VinCommand
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference
import com.mdp.innovation.obd_driving_api.enums.AvailableCommandNames
import kotlinx.android.synthetic.main.activity_pair_obd.*
import roboguice.RoboGuice
import java.io.IOException

class ConnectOBD(context: Context): ObdProgressListener {

    /**
     * Actualización del estado
     */
    override fun stateUpdate(job: ObdCommandJob) {
        val cmdName = job.command.name
        val cmdID = LookUpCommand(cmdName)
        //context.snackBarSucceso(cmdID, claContent)

        updateTripStatistic(job, cmdID = cmdID)
    }

    val TAG = javaClass.simpleName

    private var service: AbstractGatewayService? = null
    private var isServiceBound: Boolean = false
    private var preRequisites = true
    var context = context
    lateinit var appSharedPreference: SharedPreference
    var btStatus = ""
    var VIN = ""


    fun init(){
        RoboGuice.setUseAnnotationDatabases(false)
        appSharedPreference = SharedPreference(context)
        var macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        LogUtils().v(TAG, " macDevice:: $macDevice")
        startLiveData()
    }

    fun startLiveData() {

        Log.d(TAG, "Starting live data..")
        Log.d(TAG, doBindService())
        // start command execution
        Handler().post(mQueueCommands)
    }


    private val serviceConn = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            Log.d(TAG, className.toString() + " service is bound")
            isServiceBound = true
            service = (binder as AbstractGatewayService.AbstractGatewayServiceBinder).service
            service!!.setContext(this@ConnectOBD.context)
            Log.d(TAG, "Starting live data")
            try {
                service!!.startService()
                if (preRequisites)
                    btStatus = context.getString(R.string.status_bluetooth_connected)
            } catch (ioe: IOException) {
                Log.e(TAG, "Failure Starting live data")
                btStatus = context.getString(R.string.status_bluetooth_error_connecting)
                doUnbindService()
            }

        }


        // This method is *only* called when the connection to the service is lost unexpectedly
        // and *not* when the client unbinds (http://developer.android.com/guide/components/bound-services.html)
        // So the isServiceBound attribute should also be set to false when we unbind from the service.
        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG, className.toString() + " service is unbound")
            isServiceBound = false
        }
    }
    private fun queueCommands() {
        if (isServiceBound) {
            for (Command in ObdConfig.getCommands()) {
                //if (prefs.getBoolean(Command.name, true))
                service!!.queueJob(ObdCommandJob(Command))
            }
        }
    }


    fun doBindService(): String{

        if (!isServiceBound) {
            Log.d(TAG, "Binding OBD service..")
            if (preRequisites) {
                btStatus = context.getString(R.string.status_bluetooth_connecting)
                val serviceIntent = Intent(context.applicationContext, ObdGatewayService::class.java)
                context.bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE)
            } else {
                btStatus = context.getString(R.string.status_bluetooth_disabled)
                val serviceIntent = Intent(context.applicationContext, MockObdGatewayService::class.java)
                context.bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE)
            }
        }
        return btStatus
    }

    private fun doUnbindService() {
        if (isServiceBound) {
            if (service!!.isRunning) {
                service!!.stopService()
                if (preRequisites)
                    btStatus = context.getString(R.string.status_bluetooth_ok)
            }
            Log.d(TAG, "Unbinding OBD service..")
            context.unbindService(serviceConn)
            isServiceBound = false

            //OBD status
            //obdStatusTextView.setText(getString(R.string.status_obd_disconnected))
        }
    }





    /**
     * Comando de búsqueda
     */
    fun LookUpCommand(txt: String): String {
        for (item in AvailableCommandNames.values()) {
            if (item.value == txt) return item.name
        }
        return txt
    }



    private val mQueueCommands = object : Runnable {
        override fun run() {
            if (service != null && service!!.isRunning && service!!.queueEmpty()) {
                queueCommands()

            }
            // run again in period defined in preferences
            Handler().postDelayed(this, 4000)
        }
    }

    private fun updateTripStatistic(job: ObdCommandJob, cmdID: String) {
        //Log.v(TAG, " updateTripStatistic ")
        //Toast.makeText(this, "updateTripStatistic", Toast.LENGTH_LONG).show()
        if (cmdID == AvailableCommandNames.SPEED.toString()) run {
            val command = job.command as SpeedCommand
            Log.v(TAG, " Speed" + command.metricSpeed)
            //Toast.makeText(this, " Speed: " + command.metricSpeed, Toast.LENGTH_LONG).show()
            //currentTrip.setSpeedMax(command.getMetricSpeed())
        }
        if (cmdID == AvailableCommandNames.VIN.toString()) run {
            if (VIN.isEmpty()) {
                val command = job.command as VinCommand
                VIN = command.formattedResult
                Log.v(TAG, " VIN $VIN")
                //Toast.makeText(this, " VIN: $VIN", Toast.LENGTH_LONG).show()
                //currentTrip.setSpeedMax(command.getMetricSpeed())
            }else{
                if (isServiceBound){
                    doUnbindService()
                }
                Log.v(TAG, " VAR VIN: $VIN ")
                //Toast.makeText(this, " VAR VIN: $VIN", Toast.LENGTH_LONG).show()
            }
        }
    }

}