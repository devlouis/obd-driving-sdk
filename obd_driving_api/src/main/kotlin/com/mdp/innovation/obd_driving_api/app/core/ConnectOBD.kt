package com.mdp.innovation.obd_driving_api.app.core

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.service.LocationUpdatesService
import com.mdp.innovation.obd_driving_api.app.ui.config.ObdConfig
import com.mdp.innovation.obd_driving_api.app.ui.io.*
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.app.utils.UtilsLocationService
import com.mdp.innovation.obd_driving_api.commands.SpeedCommand
import com.mdp.innovation.obd_driving_api.commands.control.VinCommand
import com.mdp.innovation.obd_driving_api.commands.engine.RPMCommand
import com.mdp.innovation.obd_driving_api.data.IoTHub.SendDataOBD
import com.mdp.innovation.obd_driving_api.data.entity.TripDrivingEntity
import com.mdp.innovation.obd_driving_api.data.entity.TripEntity
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference
import com.mdp.innovation.obd_driving_api.enums.AvailableCommandNames
import io.realm.Realm
import java.io.IOException
import java.util.*
import android.provider.SyncStateContract.Helpers.update



object ConnectOBD{
    val TAG = javaClass.simpleName

    private var service: AbstractGatewayService? = null
    private var isServiceBound: Boolean = false
    var isServiceBoundLocation: Boolean = false
    private var preRequisites = true

    var obdGatewayVin: ObdGatewayVin? = null
    lateinit var appSharedPreference: SharedPreference
    var btStatus = ""
    var context: Context? = null
    lateinit var realm : Realm
    var eo = ""

    var contadorTotal = 0
    var contadorTotalLocation = 0
    var contador = 0
    var RPM = ""
    var KMH = ""


    private var macDevice = ""
    val send = SendDataOBD()

    //GPS Service
    private var mLocationUpdatesService : LocationUpdatesService? = null

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
            appSharedPreference.saveIdTrip(generateIDTrip())
            doBindService()
            // start command execution
            Handler().post(mQueueCommands)
        }else{
            obdGatewayVin!!.errorConnect(context!!.getString(R.string.mac_bluetooh_empty))
        }
    }

    fun stopLiveData(){
        doUnbindService()
        mLocationUpdatesService!!.RemoveAll()
        //doUnbindServiceLocation()
       //obdGatewayVin!!.errorConnect("Se perdio conexion al OBD")
    }

    fun stopLiveDataforError(){
        doUnbindService()
        mLocationUpdatesService!!.RemoveAll()
        //doUnbindServiceLocation()
        obdGatewayVin!!.errorConnect("Se perdio conexion al OBD")
    }

    fun generateIDTrip(): String{
        val generadorAleatorios = Random()
        var numeroAleatorio = 1+generadorAleatorios.nextInt(9999)
        LogUtils().v(TAG, " generate ID: ${numeroAleatorio})")
        return numeroAleatorio.toString()
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
            LogUtils().v(TAG, "Binding OBD service..")
            if (preRequisites) {
                btStatus = context!!.getString(R.string.status_bluetooth_connecting)
                val serviceIntent = Intent(context!!.applicationContext, ObdGatewayService::class.java)
                context!!.bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE)
            }
        }

        if (!isServiceBoundLocation){
            LogUtils().v(TAG, " Binding LOCATION Service")
            context!!.bindService(Intent(context!!.applicationContext, LocationUpdatesService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)

        }
    }

      fun doUnbindService() {
        if (isServiceBound) {
            if (service!!.isRunning) {
                service!!.stopService()
                if (preRequisites)
                    btStatus = context!!.getString(R.string.status_bluetooth_ok)
            }
            Log.d(TAG, "Unbinding OBD service..")
            context!!.unbindService(serviceConn)
            //context!!.unbindService(mServiceConnection)
            isServiceBound = false


            //OBD status
            //obdStatusTextView.setText(getString(R.string.status_obd_disconnected))
        }


    }

    fun doUnbindServiceLocation(){
        context!!.unbindService(mServiceConnection)
        isServiceBoundLocation = false
        //mLocationUpdatesService!!.RemoveAll()
    }

    fun dounbindServiceAll(){


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
            Handler().postDelayed(this, 1000)
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
                obdGatewayVin!!.getSpeedKm(KMH)
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
                }else{
                    Log.v(TAG, " VAR VIN: $VIN ")
                    if (contadorTotal == 2)
                        obdGatewayVin!!.getVin(VIN)
                }
            }
        }
        Log.v(TAG, " contador $contador")
        if (contador == 3){
            contadorTotal++
            // ENVIA AL IotHub
            //send.sendData(context, VIN, RPM, KMH, contadorTotal)
            addArticle(VIN, RPM, KMH, contadorTotal)

            contador = 0
            RPM = ""
            KMH = ""
        }
        Log.v(TAG, " contadorTotal:: $contadorTotal")

    }


    private fun addArticle(vin: String, rpm: String, kmh: String, count: Int) {
        realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        val trip = realm.createObject(TripDrivingEntity::class.java)
        trip.id = count.toLong()
        trip.id_trip = send.getIDTrip(context, vin)
        trip.kmh = kmh
        trip.rpm = rpm

        realm.commitTransaction()

        LogUtils().v(TAG, " : ${trip.toString()}")
        findAllArticle()
    }

    private fun findAllArticle() {
        var realmResults = realm.where(TripDrivingEntity::class.java).findAll()
        //realmResults.sort("id", RealmResults.SORT_ORDER_DESCENDING)
        LogUtils().v(TAG, " SIZES : ${realmResults.size}")
    }

    private fun updateArticle(id: String) {
        realm.beginTransaction()

        val article = realm.where(TripDrivingEntity::class.java).equalTo("id", id).findFirst()
       /* article.setTitle(title)
        article.setDescription(description)*/


        realm.commitTransaction()

        LogUtils().v(TAG, " : ${article.toString()}")

    }

    /**
     * Actualización del estado
     */
    @JvmStatic
    fun stateUpdate(job: ObdCommandJob, ctx: Context) {
        this.context = ctx
        val cmdName = job.command.name
        val cmdID = LookUpCommand(cmdName)
        //context.snackBarSucceso(cmdID, claContent)
        var cmdResult = ""

        if (job.state.equals(ObdCommandJob.ObdCommandJobState.EXECUTION_ERROR)) {
           // cmdResult = job.command.result
           /* if (cmdResult != null && isServiceBound) {
               // obdStatusTextView.setText(cmdResult.toLowerCase())
            }*/
        } else if (job.state.equals(ObdCommandJob.ObdCommandJobState.BROKEN_PIPE)) {
            if (isServiceBound)
                stopLiveDataforError()
        } else if (job.state.equals(ObdCommandJob.ObdCommandJobState.NOT_SUPPORTED)) {
           // cmdResult = context!!.getString(R.string.status_obd_no_support)
        } else {
            cmdResult = job.command.formattedResult
            /**
             * Esta recibiendo información...
             */
            //if (isServiceBound)
                //obdStatusTextView.setText(getString(R.string.status_obd_data))
        }

        updateTripStatistic(job, cmdID = cmdID)
    }

    @JvmStatic
    fun stateUpdateLocation(location: Location) {
        LogUtils().v(TAG, "New location_: ${UtilsLocationService().getLocationText(location)}")
        var VIN =  appSharedPreference.getVinCar()[appSharedPreference.VIN_CAR]!!
        contadorTotalLocation++
        send.sendLocation(context,VIN, location.longitude.toString(), location.latitude.toString(), contadorTotal)


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


    /**
     * GPS
     */
    // Monitors the state of the connection to the service.
    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            //val binder = binder as LocationUpdatesService.LocalBinder
            mLocationUpdatesService = (binder as LocationUpdatesService.LocalBinder).service
            mLocationUpdatesService!!.requestLocationUpdates()
            isServiceBoundLocation = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mLocationUpdatesService = null
            isServiceBoundLocation = false
        }
    }



}