package com.mdp.innovation.obd_driving_api.app.core

import android.app.Application
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
import java.io.IOException
import java.util.*
import android.provider.SyncStateContract.Helpers.update
import com.mdp.innovation.obd_driving_api.app.utils.JSONUtils
import com.mdp.innovation.obd_driving_api.data.entity.LocationEntity
import com.mdp.innovation.obd_driving_api.data.entity.ObdEntity
import com.mdp.innovation.obd_driving_api.data.store.TripRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.LocationRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.ObdRepository
import java.lang.Exception
import java.text.SimpleDateFormat


object ConnectOBD{
    val TAG = javaClass.simpleName
    val TAG_BD = " BD_LOCAL"
    val TAG_GET = " TAG_GET"

    val LIMIT = 20

    private var service: AbstractGatewayService? = null
    private var isServiceBound: Boolean = false
    var isServiceBoundLocation: Boolean = false
    private var preRequisites = true

    var obdGatewayVin: ObdGatewayVin? = null
    lateinit var appSharedPreference: SharedPreference
    var btStatus = ""
    var context: Context? = null

    var eo = ""

    var contadorTotal = 0
    var contadorTotalLocation = 0
    var contador = 0
    var RPM = ""
    var KMH = ""

    var statusTrip = "0"


    private var macDevice = ""
    var VIN = ""
    val send = SendDataOBD()

    //GPS Service
    private var mLocationUpdatesService : LocationUpdatesService? = null

    fun initialize(context: Context) {
        this.context = context

        LogUtils().v(TAG, " INIT")
        //RoboGuice.setUseAnnotationDatabases(false)
        appSharedPreference = SharedPreference(context)
        macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        VIN =  appSharedPreference.getVinCar()[appSharedPreference.VIN_CAR]!!
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
        statusTrip = "0"

        appSharedPreference.saveIdRawBD("0")
        Log.d(TAG, "Starting live data...")
        this.obdGatewayVin = mObdGatewayVin
        if (macDevice.isNotEmpty()) {
            appSharedPreference.saveIdTrip(generateIDTrip())
            doBindService()
            // start command execution
            Handler().post(mQueueCommands)
            Handler().post(mSyncronizarBDtoIothub)
        }else{
            obdGatewayVin!!.errorConnect(context!!.getString(R.string.mac_bluetooh_empty))
        }
    }

    fun stopLiveData(){
        statusTrip = "2"
        Handler().postDelayed({
            doUnbindService()
            mLocationUpdatesService!!.RemoveAll()
        }, 1000)


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
            val sdf7 = SimpleDateFormat("H:mm:ss:SSS")
            val currentDateandTimeFull = sdf7.format(Date())
            LogUtils().v(TAG_GET, " GET OBD: ${currentDateandTimeFull} :: ${RPM},${KMH} ")

            //Log.v(TAG_BD, " contador OBD :: $contadorTotal")
            addToBdObd(VIN, RPM, KMH, contadorTotal)

            contador = 0
            RPM = ""
            KMH = ""
        }


    }


    private fun addToBdObd(vin: String, rpm: String, kmh: String, count: Int) {
        val sdf6 = SimpleDateFormat("H:mm:ss")
        val sdf7 = SimpleDateFormat("H:mm:ss.SSS")
        val sdf8 = SimpleDateFormat("SSS")
        val currentDateandTime = sdf6.format(Date())

        LogUtils().v(TAG, " id viaje : error - no se encuentra - se registra")
        val obdEntity = ObdEntity()
        obdEntity.id_trip = send.getIDTrip(context, vin)
        obdEntity.kmh = kmh
        obdEntity.rpm = rpm
        obdEntity.dataNew = currentDateandTime
        obdEntity.status = statusTrip
        ObdRepository(Application()).addObd(obdEntity)
        LogUtils().v(TAG_BD, " OBD NEW : ${currentDateandTime} = ${obdEntity.toString()}")
    }


    private fun addToBdLocation(id: String, location: Location) {

        val sdf6 = SimpleDateFormat("H:mm:ss")
        val sdf7 = ("H:mm:ss.SSS")
        val currentDateandTime = sdf6.format(Date())
        val currentDateandTimeFull = sdf7.format(Date())
        //LogUtils().v(TAG_GET, " GET LOCATION: ${currentDateandTimeFull} :: ${location.longitude},${location.latitude} ")

        val locationEntity = LocationEntity()
        locationEntity.id_trip = send.getIDTrip(context, VIN)
        locationEntity.longitud = location.longitude.toString()
        locationEntity.latitudd = location.latitude.toString()
        locationEntity.dataNew = currentDateandTime
        locationEntity.status = statusTrip
        LocationRepository(Application()).addLocation(locationEntity)
        LogUtils().v(TAG_BD, " LOCATION NEW time: ${currentDateandTime} = ${locationEntity.toString()}")

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
        LogUtils().v(TAG_BD, "New location_: ${UtilsLocationService().getLocationText(location)}")
        contadorTotalLocation++
        val id = appSharedPreference.getIdRawBD()
        addToBdLocation(id[appSharedPreference.ID_RAW_BD]!!, location)



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


    //TODO ###########################################################################################
    //TODO ###########################################################################################
    //TODO ###########################################################################################
    /**
     * Sincronizacion BD to IoTHub
     */
    /**
     * Enviar a IoTHub cada 1 min
     */
    val handler = Handler()
    private var mSyncronizarBDtoIothub = object : Runnable {
        override fun run() {
            var countBD = 0
            if (start){
                LogUtils().v(TAG_BD, "########################### I N I C I O ####################################")
                getFirst20OBD()
            }
            start = true

            LocationRepository(Application()).getAll(object : LocationRepository.PopulateCallback{
                override fun onSuccess(locationEntityList: MutableList<LocationEntity>) {
                    countBD = locationEntityList.size
                    LogUtils().v(TAG_BD, " Count Location: ${countBD}")

                }
                override fun onFailure(e: Exception?) {

                }
            })

            /**
             * Validar si todavia existe data que sincronizar
             */
            if (countBD > 0){
                handler.postDelayed(this, 30000)
            }
            else{
                handler.removeCallbacks(this)
                LogUtils().v(TAG_BD, " Termina la sincronizacion a la BD")
            }

        }
    }

    /**
     * Obtener los 20 primeros
     */
    var firstOBD = 0
    var firstLocation = 0
    var start = false
    private fun getFirst20OBD(){

        ObdRepository(Application()).getFirtsTrips(LIMIT, object : ObdRepository.PopulateCallback{
            override fun onSuccess(obdEntityList: MutableList<ObdEntity>) {
                for (obd in obdEntityList){
                    firstOBD++
                    validateToSend(obd.dataNew, obd, "OBD")
                    LogUtils().v(TAG_BD, " firstOBD : ${firstOBD}")
                }
                firstOBD = 0
                ObdRepository(Application()).deleteFirstObd(LIMIT)
                LogUtils().v(TAG_BD, "SE BORRO LOS 20 PRIMERO OBD...")
                getFirst20Location()
            }

            override fun onFailure(e: Exception?) {
                LogUtils().v(TAG_BD, "No se encuentras mas datos...")
            }
        })
    }

    private fun getFirst20Location(){
        LocationRepository(Application()).getFirtsTrips(LIMIT, object : LocationRepository.PopulateCallback {
            override fun onSuccess(locationEntityList: MutableList<LocationEntity>) {
                for (location in locationEntityList){
                    firstLocation++
                    validateToSend(location.dataNew, location, "LOCATION")
                    LogUtils().v(TAG_BD, " firstLocation : ${firstLocation}")
                }
                firstLocation = 0
                LocationRepository(Application()).deleteFirstLocation(LIMIT)
                LogUtils().v(TAG_BD, "SE BORRO LOS 20 PRIMERO LOCATION...")
                LogUtils().v(TAG_BD, "######################### F I N ######################################")

                /**
                 * Enviar a IoTHub
                 */
                //

            }

            override fun onFailure(e: Exception?) {
                LogUtils().v(TAG_BD, "No se encuentras mas datos...")
            }
        })
    }


    private fun validateToSend(date: String, any: Any, table: String){
        TripRepository(Application()).getWhereDate(date, object : TripRepository.GetWhenDateCallback {
          override fun onSuccess(trip: TripEntity) {
              LogUtils().v(TAG_BD, " id viaje : ${trip.toString()}")
              when(table){
                  "OBD" ->  {
                      var value = any as ObdEntity
                      trip.id_trip = value.id_trip
                      trip.kmh = value.kmh
                      trip.rpm = value.rpm
                      trip.dataUdate = value.dataNew
                      trip.status = value.status
                      TripRepository(Application()).update(trip)
                  }
                  "LOCATION" ->{
                      var value = any as LocationEntity
                      LogUtils().v(TAG_BD, " LOCATION - value : ${value.toString()}")
                      trip.id_trip = value.id_trip
                      trip.latitudd = value.latitudd
                      trip.longitud = value.longitud
                      trip.dataUdate = value.dataNew
                      trip.status = value.status
                      TripRepository(Application()).update(trip)
                  }
              }
              LogUtils().v(TAG_BD, " LOCATION BD update : ${trip.toString()}")

          }
          override fun onFailure() {
              LogUtils().v(TAG, " id viaje : error - no se encuentra - se registra")
              when(table){
                  "OBD" ->  {
                      var value = any as ObdEntity
                      val trip = TripEntity()
                      trip.id_trip = value.id_trip
                      trip.kmh = value.kmh
                      trip.rpm = value.rpm
                      trip.dataNew = value.dataNew
                      trip.status = value.status
                      TripRepository(Application()).add(trip)
                      LogUtils().v(TAG_BD, " OBD NEW INSERT: ${trip.toString()}")
                  }
                  "LOCATION" ->{
                      var value = any as LocationEntity
                      val trip = TripEntity()
                      trip.id_trip = value.id_trip
                      trip.latitudd = value.latitudd
                      trip.longitud = value.longitud
                      trip.dataNew = value.dataNew
                      trip.status = value.status
                      TripRepository(Application()).add(trip)
                      LogUtils().v(TAG_BD, " LOCATION NEW INSERT: ${trip.toString()}")
                  }
              }
          }
        })
    }


    var limit = 0
    var limit2 = 20

    private fun getFirstTripSend(){
        var countTrip = getAllTrip()
        TripRepository(Application()).getFirtsTrips(limit , limit2, object : TripRepository.PopulateCallback {
            override fun onSuccess(tripEntityList: MutableList<TripEntity>?) {
                SendDataOBD().sendDataJsonString(JSONUtils.generateJSONArray(tripEntityList).toString())
                limit+20
                limit2+20
                if (countTrip < limit2){

                }
            }
            override fun onFailure(e: Exception?) {

            }
        })
    }

    private fun getAllTrip():Int {
        var count = 0
        TripRepository(Application()).getAllNotes(object : TripRepository.PopulateCallback {
            override fun onSuccess(tripEntityList: MutableList<TripEntity>) {
                count = tripEntityList.size
            }
            override fun onFailure(e: Exception?) {

            }
        })
        return count
    }

}