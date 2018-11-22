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
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
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

object ConnectOBD{
    val TAG = javaClass.simpleName

    private var service: AbstractGatewayService? = null
    private var isServiceBound: Boolean = false
    private var preRequisites = true

    lateinit var obdGatewayVin: ObdGatewayVin
    lateinit var appSharedPreference: SharedPreference
    var btStatus = ""
    var context: Context? = null
    var eo = ""

    var macDevice = ""


    fun initialize(context: Context){
        this.context = context
        LogUtils().v(TAG, " INIT")
        RoboGuice.setUseAnnotationDatabases(false)
        appSharedPreference = SharedPreference(context)
        //macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        LogUtils().v(TAG, " macDevice:: $macDevice")
    }

    /**
     * Verificar si la mac del OBD esta guardado.
     */
    data class Result(val result: Boolean, val macBluetooth: String)
    fun verifyMacOBD():Result{
        //var macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        LogUtils().v(TAG, " macDevice:: $macDevice")
        return Result(macDevice.isNotEmpty(), macDevice)
    }

    fun startLiveData(mObdGatewayVin: ObdGatewayVin) {
        Log.d(TAG, "Starting live data...")
        this.obdGatewayVin = mObdGatewayVin
        if (macDevice.isNotEmpty()) {
            doBindService()
            // start command execution
            Handler().post(mQueueCommands)
        }else{
            obdGatewayVin.errorConnect(context!!.getString(R.string.mac_bluetooh_empty))
        }
    }

    fun stopLiveData(){
        doUnbindService()
    }

    val serviceConn = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            Log.d(TAG, className.toString() + " service is bound")
            isServiceBound = true
            service = (binder as AbstractGatewayService.AbstractGatewayServiceBinder).service
            service!!.setContext(context)
            Log.d(TAG, "onServiceConnected")
            try {
                service!!.startService()
                if (preRequisites)
                    btStatus = context!!.getString(R.string.status_bluetooth_connected)
            } catch (ioe: IOException) {
                LogUtils().v(TAG, "Failure Starting live data ${ioe.message.toString()}" )
                btStatus = context!!.getString(R.string.status_bluetooth_error_connecting)
                doUnbindService()
                obdGatewayVin.errorConnect(context!!.getString(R.string.status_bluetooth_error_connecting))

            }

        }


        // This method is *only* called when the connection to the service is lost unexpectedly
        // and *not* when the client unbinds (http://developer.android.com/guide/components/bound-services.html)
        // So the isServiceBound attribute should also be set to false when we unbind from the service.
        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG, className.toString() + " service is unbound")
            service!!.setContext(this@ConnectOBD.context)
            eo = "banna"
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


    private fun doBindService(){

        if (!isServiceBound) {
            Log.d(TAG, "Binding OBD service..")
            if (preRequisites) {
                btStatus = context!!.getString(R.string.status_bluetooth_connecting)
                val serviceIntent = Intent(context!!.applicationContext, ObdGatewayService::class.java)
                context!!.bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE)
            } else {
                btStatus = context!!.getString(R.string.status_bluetooth_disabled)
                val serviceIntent = Intent(context!!.applicationContext, MockObdGatewayService::class.java)
                serviceIntent.putExtra("OB","BOLI")
                context!!.bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE)
            }
        }
    }

     private fun doUnbindService() {
        if (isServiceBound) {
            if (service!!.isRunning) {
                service!!.stopService()
                if (preRequisites)
                    btStatus = context!!.getString(R.string.status_bluetooth_ok)
            }
            Log.d(TAG, "Unbinding OBD service..")
            context!!.unbindService(serviceConn)
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
        //Log.v(TAG, " VINnn ${context!!.getString(R.string.status_bluetooth_connecting)}")
        var VIN = ""
        if (cmdID == AvailableCommandNames.SPEED.toString()) {
            val command = job.command as SpeedCommand
            Log.v(TAG, " Speed" + command.metricSpeed)
            //Toast.makeText(this, " Speed: " + command.metricSpeed, Toast.LENGTH_LONG).show()
            //currentTrip.setSpeedMax(command.getMetricSpeed())
        }
        if (cmdID == AvailableCommandNames.VIN.toString())  {
            if (VIN.isEmpty()) {
                Log.v(TAG, " VIN;;; $VIN")
                val command = job.command as VinCommand
                VIN = command.formattedResult

                Log.v(TAG, " VIN $VIN")
                obdGatewayVin.getVin(VIN)
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



    /**
     * Actualización del estado
     */
    @JvmStatic
    fun stateUpdate(job2: ObdCommandJob, ctx: Context) {
        this.context = ctx
        val cmdName = job2.command.name
        val cmdID = LookUpCommand(cmdName)
        //context.snackBarSucceso(cmdID, claContent)
        updateTripStatistic(job2, cmdID = cmdID)
    }

    /**
     * Verificar service OBD
     */
    fun CheckConecction(): Boolean{
        return isServiceBound
    }

    /**
     * DUMMY
     */
    fun getVInDummy():String{
        return "M4N4N4T3CU3NT0"
    }
}