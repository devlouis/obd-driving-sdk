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
import com.mdp.innovation.obd_driving_api.commands.engine.RPMCommand
import com.mdp.innovation.obd_driving_api.data.Broadcast.OBDRestarBroadcastReceiver
import com.mdp.innovation.obd_driving_api.data.IoTHub.SendDataOBD
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference
import com.mdp.innovation.obd_driving_api.enums.AvailableCommandNames
import kotlinx.android.synthetic.main.activity_pair_obd.*
import java.io.IOException

object ConnectOBD{
    val TAG = javaClass.simpleName

    private var service: AbstractGatewayService? = null
    private var isServiceBound: Boolean = false
    private var preRequisites = true

    var obdGatewayVin: ObdGatewayVin? = null
    lateinit var appSharedPreference: SharedPreference
    var btStatus = ""
    var context: Context? = null
    var eo = ""

    var contadorTotal = 0
    var contador = 0
    var RPM = ""
    var KMH = ""


    private var macDevice = ""
    val send = SendDataOBD()

    fun initialize(context: Context){
        this.context = context
        LogUtils().v(TAG, " INIT")
        //RoboGuice.setUseAnnotationDatabases(false)
        appSharedPreference = SharedPreference(context)
        macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        send.InitClient()
        LogUtils().v(TAG, " macDevice:: $macDevice")
        LogUtils().v(TAG, " obdGatewayVin:: $obdGatewayVin")

    }

    /**
     * Verificar si la mac del OBD esta guardado.
     */
    data class Result(val result: Boolean, val macBluetooth: String)
    fun verifyMacOBD():Result{
        LogUtils().v(TAG, " macDevice:: $macDevice")
        macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
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
            obdGatewayVin!!.errorConnect(context!!.getString(R.string.mac_bluetooh_empty))
        }
    }

    fun stopLiveData(){
        doUnbindService()
    }

    @JvmStatic
    fun startmQueue(){
        Log.d(TAG, "Starting live data... 2")
        if (macDevice.isNotEmpty()) {
            doBindService()
            // start command execution
            Handler().post(mQueueCommands)
        }else{
            //obdGatewayVin!!.errorConnect(context!!.getString(R.string.mac_bluetooh_empty))
        }
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
                obdGatewayVin!!.errorConnect(context!!.getString(R.string.status_bluetooth_error_connecting))

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
            for (Command in ObdConfig.getCommandCustom()) {
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
            Handler().postDelayed(this, 2000)
        }
    }

    private fun updateTripStatistic(job: ObdCommandJob, cmdID: String) {
        var VIN =  appSharedPreference.getVinCar()[appSharedPreference.VIN_CAR]!!
        Log.v(TAG, " antes del wheb")
        when(cmdID){
            AvailableCommandNames.SPEED.toString() -> {
                contador++
                val command = job.command as SpeedCommand
                KMH = command.metricSpeed.toString()
                Log.v(TAG, " Speed: ${command.metricSpeed}")
            }
            AvailableCommandNames.ENGINE_RPM.toString() -> {
                contador++
                val command = job.command as RPMCommand
                RPM = command.rpm.toString()
                Log.v(TAG, " Rpm: ${command.rpm}")
            }
            AvailableCommandNames.VIN.toString() -> {
                contador++
                if (VIN.isEmpty()) {
                    val command = job.command as VinCommand
                    VIN = command.formattedResult
                    Log.v(TAG, " VIN $VIN")
                    appSharedPreference.saveVinCar(VIN)
                    //obdGatewayVin!!.getVin(VIN)
                }else{
                    Log.v(TAG, " VAR VIN: $VIN ")
                }
            }
        }
        Log.v(TAG, " contador $contador")
        if (contador == 3){
            contadorTotal++
            send.sendData(VIN, RPM, KMH)
            contador = 0
            RPM = ""
            KMH = ""
        }
        Log.v(TAG, " contadorTotal:: $contadorTotal")

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

    fun restarServiceOBD(){
        Log.v(" OBDRestar ", "broadcast 2")
        //service!!.setContext(context)
        service!!.onDestroy()

    }

}