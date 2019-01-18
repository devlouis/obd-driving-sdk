package com.mdp.innovation.obd_driving_api.app.core

import android.annotation.SuppressLint
import android.app.Application
import android.content.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD.initSendDataBD
import com.mdp.innovation.obd_driving_api.app.core.service.LocationUpdatesService
import com.mdp.innovation.obd_driving_api.app.ui.config.ObdConfig
import com.mdp.innovation.obd_driving_api.app.ui.io.*
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.app.utils.UtilsLocationService
import com.mdp.innovation.obd_driving_api.commands.SpeedCommand
import com.mdp.innovation.obd_driving_api.commands.control.VinCommand
import com.mdp.innovation.obd_driving_api.commands.engine.RPMCommand
import com.mdp.innovation.obd_driving_api.data.IoTHub.SendDataOBD
import com.mdp.innovation.obd_driving_api.data.entity.TripEntity
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference
import com.mdp.innovation.obd_driving_api.enums.AvailableCommandNames
import java.io.IOException
import java.util.*

import com.mdp.innovation.obd_driving_api.app.utils.JSONUtils
import com.mdp.innovation.obd_driving_api.data.entity.LocationEntity
import com.mdp.innovation.obd_driving_api.data.entity.ObdEntity
import com.mdp.innovation.obd_driving_api.data.store.TripRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.LocationRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.ObdRepository
import java.lang.Exception
import java.text.SimpleDateFormat


@SuppressLint("StaticFieldLeak")
object ConnectOBD{
    val TAG = javaClass.simpleName
    val TAG_BD = " BD_LOCAL"
    val TAG_GET = " TAG_GET"

    val LIMIT = 30

    val OBD_LOST = 404
    val OBD_ERROR = 401
    val OBD_NO_PAIRED = 301

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
    var initSendDataBD = false
    var firstSend = false
    var contadorTotalLocation = 0
    var contador = 0
    var RPM = ""
    var KMH = ""

    var statusTrip = "0"

    var handler = Handler()
    private var macDevice = ""
    var VIN = ""
    val send = SendDataOBD()

    var statusContinueTrip = false

    val PACKAGE_NAME = "com.mdp.innovation.obd_driving_api.app.core"
    val EXTRA_SPEED = "${PACKAGE_NAME}.speed"
    val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"

    //GPS Service
    private var mLocationUpdatesService : LocationUpdatesService? = null



    fun initialize(context: Context) {
        this.context = context
        //todo Crashlytics.getInstance().crash() // Force a crash
        LogUtils().v(TAG, " INIT")
        //RoboGuice.setUseAnnotationDatabases(false)
        appSharedPreference = SharedPreference(context)
        macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        VIN =  appSharedPreference.getVinCar()[appSharedPreference.VIN_CAR]!!
        Handler().postDelayed(initClientIotHub, 2000)
        //send.sendDataJsonString("")
        LogUtils().v(TAG, " macDevice:: $macDevice")
        LogUtils().v(TAG, " obdGatewayVin:: $obdGatewayVin")



    }

    private val initClientIotHub = object : Runnable {
        override fun run() {
            send.InitClient()
        }
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
        /**
         *
         */
        contadorTotal = 0

        limit = 0
        limit2 = LIMIT
        TripRepository(Application()).deleteAll()
        ObdRepository(Application()).deleteAll()
        LocationRepository(Application()).deleteAll()
        start = false

        contadorTotalLocation = 0
        statusTrip = "0"
        initSendDataBD = false

        appSharedPreference.saveIdRawBD("0")
        Log.d(TAG, "Starting live data...")
        this.obdGatewayVin = mObdGatewayVin
        if (macDevice.isNotEmpty()) {
            doBindService()
            // start command execution
            Handler().post(mQueueCommands)
            handler.post(mSyncronizarBDtoIothub)
        }else{
            obdGatewayVin!!.errorConnect(context!!.getString(R.string.mac_bluetooh_empty), OBD_NO_PAIRED)
        }
    }

    fun stopLiveData(){

        statusTrip = "2"
        Handler().postDelayed({
            LogUtils().v(TAG_BD, "######################### POST 1.5 SEG ######################################")
            doUnbindService()
            mLocationUpdatesService!!.removeLocationUpdates()
            /**
             * Enviar Data restante
             * @param mSyncronizarBDtoIothub remuevo el handler
             */
            handler.removeCallbacks(mSyncronizarBDtoIothub)

            Handler().postDelayed({ getAllOBD() },1000)

        }, 1500)
    }

    fun stopLiveDataforError(){

        doUnbindService()
        mLocationUpdatesService!!.RemoveAll()
        //doUnbindServiceLocation()

        /**
         * Inicia el tiempo de espera para reconexion
         */
        startTimerContinue()

        obdGatewayVin!!.errorConnect("Se perdio conexion al OBD", OBD_LOST)





    }


    var secondsRemaining: Long = 5
    var timerContinue = object : CountDownTimer(secondsRemaining * 1000, 1000) {
        override fun onFinish() {
            statusContinueTrip = false
            LogUtils().v(TAG_BD, " COULDDOWN: ${secondsRemaining} - TERMINO TIEMPO DE ESPERA")
        }

        override fun onTick(millisUntilFinished: Long) {
            statusContinueTrip = true
            LogUtils().v(TAG_BD, " COULDDOWN: ${millisUntilFinished / 1000}")

        }
    }

    private fun startTimerContinue(){
        LogUtils().v(TAG_BD, " startTimerContinue ...")

        timerContinue.start()

    }




    fun generateIDTrip(): String{
        val generadorAleatorios = Random()
        var numeroAleatorio = 1+generadorAleatorios.nextInt(9999)
        LogUtils().v(TAG, " generate ID: ${numeroAleatorio})")
        return numeroAleatorio.toString()
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
                mLocationUpdatesService!!.requestLocationUpdates()
           /*     Log.d(TAG_BD, " isServiceBoundLocation: ${isServiceBoundLocation}")
                if (!isServiceBoundLocation){
                    LogUtils().v(TAG, " Binding LOCATION Service")
                    context!!.bindService(Intent(context!!.applicationContext, LocationUpdatesService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
                    //else

                }else{
                    mLocationUpdatesService!!.requestLocationUpdates()
                }*/

                if (preRequisites)
                    btStatus = context!!.getString(R.string.status_bluetooth_connected)
            } catch (ioe: IOException) {
                LogUtils().v(TAG, "Failure Starting live data ${ioe.message.toString()}" )
                btStatus = context!!.getString(R.string.status_bluetooth_error_connecting)

                doUnbindService()
                obdGatewayVin!!.errorConnect(context!!.getString(R.string.status_bluetooth_error_connecting), OBD_ERROR)

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

        if (!isServiceBoundLocation){
            LogUtils().v(TAG, " Binding LOCATION Service")
            context!!.bindService(Intent(context!!.applicationContext, LocationUpdatesService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
        }


        if (!isServiceBound) {
            LogUtils().v(TAG, "Binding OBD service..")
            if (preRequisites) {
                btStatus = context!!.getString(R.string.status_bluetooth_connecting)
                val serviceIntent = Intent(context!!.applicationContext, ObdGatewayService::class.java)
                context!!.bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE)
            }
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
                if (contadorTotal >= 2){
                    //obdGatewayVin!!.getSpeedKm(KMH)
                    val intent = Intent(ACTION_BROADCAST)
                    intent.putExtra(EXTRA_SPEED, KMH)
                    LocalBroadcastManager.getInstance(Application()).sendBroadcast(intent)
                }
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
                val command = job.command as VinCommand

                if (VIN != command.formattedResult){
                    VIN = command.formattedResult
                    Log.v(TAG, " VIN $VIN")
                    appSharedPreference.saveVinCar(VIN)
                }else
                    Log.v(TAG, " VAR VIN: $VIN ")


                if (contadorTotal == 2){
                    obdGatewayVin!!.getVin(VIN)
                    statusTrip = "0"
                    initSendDataBD = true
                    firstSend = true
                    Log.d(TAG_BD, " isServiceBoundLocation: ${isServiceBoundLocation}")



                }else{
                    if (statusTrip != "2")
                    statusTrip = "1"
                }


            }
        }
        Log.v(TAG, " contador $contador")
            if (contador == 3) {
                contador = 0
                contadorTotal++
                /**
                 * Inicia el envio de dato BD Local
                 * @param initSendDataBD = true
                 */
                if (initSendDataBD) {
                    val sdf7 = SimpleDateFormat("H:mm:ss:SSS")
                    val currentDateandTimeFull = sdf7.format(Date())
                    LogUtils().v(TAG_GET, " GET OBD: ${currentDateandTimeFull} :: ${RPM},${KMH} ")
                    addToBdObd(VIN, RPM, KMH, contadorTotal)
                }

            }



    }


    private fun addToBdObd(vin: String, rpm: String, kmh: String, count: Int) {
        val sdf6 = SimpleDateFormat("H:mm:ss")
        val currentDateandTime = sdf6.format(Date())

        val sdf = SimpleDateFormat("yyyy:MM:dd")
        val currentToDay = sdf.format(Date())


        val obdEntity = ObdEntity()
        obdEntity.id_trip = send.getIDTrip(context, vin)
        obdEntity.vin = vin
        obdEntity.kmh = kmh
        obdEntity.rpm = rpm
        obdEntity.dataNew = currentToDay.replace(":","-") +" "+ currentDateandTime
        obdEntity.status = statusTrip
        if (sensorAccelerometer != null){
            obdEntity.ax = sensorAccelerometer!!.values[0]
            obdEntity.ay = sensorAccelerometer!!.values[1]
            obdEntity.az = sensorAccelerometer!!.values[2]
        }

        ObdRepository(Application()).addObd(obdEntity)
        LogUtils().v(TAG_BD, " OBD ADD : ${currentDateandTime} = ${obdEntity.toString()}")
    }


    private fun addToBdLocation(location: Location) {
        val sdf6 = SimpleDateFormat("H:mm:ss")
        val currentDateandTime = sdf6.format(Date())

        val sdf = SimpleDateFormat("yyyy:MM:dd")
        val currentToDay = sdf.format(Date())

     /*   if (firstSend) {
            statusTrip = "0"
            firstSend = false
        }else{
            if (statusTrip != "2")
                statusTrip = "1"
        }
*/

        val locationEntity = LocationEntity()
        locationEntity.id_trip = send.getIDTrip(context, VIN)
        locationEntity.longitud = location.longitude.toString()
        locationEntity.latitudd = location.latitude.toString()
        locationEntity.bearing = location.bearing.toString()
        locationEntity.dataNew = currentToDay.replace(":","-") +" "+ currentDateandTime
        locationEntity.status = statusTrip
        LocationRepository(Application()).addLocation(locationEntity)
        LogUtils().v(TAG_BD, " LOCATION ADD: ${currentDateandTime} = ${locationEntity.toString()}")

    }

    /**
     * Actualización del estado
     */
    private var sensorAccelerometer: SensorEvent? = null
    @JvmStatic
    fun stateUpdate(job: ObdCommandJob, sensorAccelerometer: SensorEvent,  ctx: Context) {
        this.context = ctx
        this.sensorAccelerometer = sensorAccelerometer
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
        addToBdLocation(location)
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

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            //val binder = binder as LocationUpdatesService.LocalBinder
           /* mLocationUpdatesService = (binder as LocationUpdatesService.LocalBinder).service
            mLocationUpdatesService!!.requestLocationUpdates()*/

            val binder = service as LocationUpdatesService.LocalBinder
            mLocationUpdatesService = binder.service
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

    private var mSyncronizarBDtoIothub = object : Runnable {
        override fun run() {
            if (start){
                LogUtils().v(TAG_BD, "########################### I N I C I O ####################################")
                //validateMoreData()
                getFirst20OBD()

            }
            handler.postDelayed(this, 30000)
            start = true
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

                Handler().postDelayed(sendTripIotHub,3000)


            }

            override fun onFailure(e: Exception?) {
                LogUtils().v(TAG_BD, "No se encuentras mas datos...")
            }
        })
    }

    private val sendTripIotHub = object : Runnable {
        override fun run() {
            getFirstTripSend()

            //Handler().postDelayed(this, 2000)
        }
    }

    private fun validateToSend(date: String, any: Any, table: String){
        TripRepository(Application()).getWhereDate(date, object : TripRepository.GetWhenDateCallback {
          override fun onSuccess(trip: TripEntity) {
              LogUtils().v(TAG_BD, " id viaje : ${trip.toString()}")
              when(table){
                  "OBD" ->  {
                      var value = any as ObdEntity
                      LogUtils().v(TAG_BD, " OBD - value : ${value.toString()}")
                      trip.tripId = value.id_trip
                      trip.vin = value.vin
                      if (value.kmh.isNotEmpty()){
                          trip.speed =  value.kmh.toInt()
                      }else{
                          trip.speed =  -1
                      }
                      trip.rpm = value.rpm.toInt()
                      trip.dataUdate = value.dataNew
                      trip.status = value.status
                      trip.ax = value.ax
                      trip.ay = value.ay
                      trip.az = value.az
                      TripRepository(Application()).update(trip)
                      LogUtils().v(TAG_BD, " OBD BD update : ${trip.toString()}")

                  }
                  "LOCATION" ->{
                      var value = any as LocationEntity
                      LogUtils().v(TAG_BD, " LOCATION - value : ${value.toString()}")
                      trip.tripId = value.id_trip
                      trip.lat = value.latitudd.toFloat()
                      trip.lon = value.longitud.toFloat()
                      trip.bearing = value.bearing.toFloat()
                      trip.dataUdate = value.dataNew
                      trip.status = value.status
                      TripRepository(Application()).update(trip)
                      LogUtils().v(TAG_BD, " LOCATION BD update : ${trip.toString()}")
                  }
              }


          }
          override fun onFailure() {
              //LogUtils().v(TAG, " id viaje : error - no se encuentra - se registra")
              when(table){
                  "OBD" ->  {
                      var value = any as ObdEntity
                      val trip = TripEntity()
                      trip.tripId = value.id_trip
                      trip.vin = value.vin

                      if (value.kmh.isNotEmpty()){
                          trip.speed =  value.kmh.toInt()
                      }else{
                          trip.speed =  -1
                      }

                      trip.rpm = value.rpm.toInt()
                      trip.time = value.dataNew
                      trip.status = value.status
                      trip.ax = value.ax
                      trip.ay = value.ay
                      trip.az = value.az
                      TripRepository(Application()).add(trip)
                      LogUtils().v(TAG_BD, " OBD NEW INSERT: ${trip.toString()}")
                  }
                  "LOCATION" ->{
                      var value = any as LocationEntity
                      val trip = TripEntity()
                      trip.tripId = value.id_trip
                      trip.lat = value.latitudd.toFloat()
                      trip.lon = value.longitud.toFloat()
                      trip.bearing = value.bearing.toFloat()
                      trip.time = value.dataNew
                      trip.status = value.status
                      TripRepository(Application()).add(trip)
                      LogUtils().v(TAG_BD, " LOCATION NEW INSERT: ${trip.toString()}")
                  }
              }
          }
        })
    }


    var limit = 0
    var limit2 = LIMIT
    var countTotalTrip = 0
    var countTotalTripPost = 0

    private fun getFirstTripSend(){
        TripRepository(Application()).getFirtsTrips(limit , limit2, object : TripRepository.PopulateCallback {
            override fun onSuccess(tripEntityList: MutableList<TripEntity>) {
                LogUtils().v(TAG_BD," limit: $limit  - limit2: $limit2")
                LogUtils().v(TAG_BD," TRIP SIZE: ${tripEntityList.size}")
                LogUtils().v(TAG_BD," tripEntityList: ${tripEntityList.toString()}")
                if (tripEntityList.size == 0){
                    handler.removeCallbacks(mSyncronizarBDtoIothub)
                    LogUtils().v(TAG_BD, "##### Termina la sincronizacion a la BD #####")
                }else{
                    limit = (limit2 - (LIMIT - tripEntityList.size))
                    limit2 = limit + LIMIT
                    send.sendDataJsonString(JSONUtils.generateJSONArray(tripEntityList).toString())
                }
            }
            override fun onFailure(e: Exception?) {

            }
        })
    }

    fun getAllTrip(){
        var count = 0
        TripRepository(Application()).getAllNotes(object : TripRepository.PopulateCallback {
            override fun onSuccess(tripEntityList: MutableList<TripEntity>) {
                count = tripEntityList.size
                LogUtils().v(TAG_BD, " Datos restantes : ${count}")
                LogUtils().v(TAG_BD, " Datos tripEntityList : ${tripEntityList.toString()}")
                LogUtils().v(TAG_BD, " Datos tripEntityList ultimo: ${tripEntityList[count-1]}")
                Toast.makeText(context, "ULTIMO STATUS ${tripEntityList[count-1].status} -- HORA ${tripEntityList[count-1].time}", Toast.LENGTH_LONG).show()
                //getAllOBD()
            }
            override fun onFailure(e: Exception?) {

            }
        })
    }

    private fun getAllOBD(){
        LogUtils().v(TAG_BD, "######################### I N I C I O   U L T I M A ######################################")
        ObdRepository(Application()).getAll(object : ObdRepository.PopulateCallback{
            override fun onSuccess(obdEntityList: MutableList<ObdEntity>) {
                for (obd in obdEntityList){
                    firstOBD++
                    validateToSend(obd.dataNew, obd, "OBD")
                    LogUtils().v(TAG_BD, " firstOBD : ${firstOBD}")
                    LogUtils().v(TAG_BD, " obdEntityList : ${obdEntityList.size}")
                }
                firstOBD = 0
                ObdRepository(Application()).deleteFirstObd(obdEntityList.size)
                LogUtils().v(TAG_BD, "SE BORRO LOS ${obdEntityList.size} DEL OBD...")
                getAllLocation()
            }

            override fun onFailure(e: Exception?) {

            }
        })
    }

    private fun getAllLocation(){

        LocationRepository(Application()).getAll(object : LocationRepository.PopulateCallback{
            override fun onSuccess(locationEntityList: MutableList<LocationEntity>) {
                for (location in locationEntityList){
                    firstLocation++
                    validateToSend(location.dataNew, location, "LOCATION")
                    LogUtils().v(TAG_BD, " firstLocation : ${firstLocation}")
                    LogUtils().v(TAG_BD, " locationEntityList : ${locationEntityList.size}")
                }
                firstLocation = 0
                LocationRepository(Application()).deleteFirstLocation(locationEntityList.size)
                LogUtils().v(TAG_BD, "SE BORRO LOS ${locationEntityList.size} DEL OBD...")
                LogUtils().v(TAG_BD, "######################### F I N   U L T I M A ######################################")
                getAllTrip()
                /**
                 * Enviar a IoTHub
                 */
                Handler().postDelayed(sendTripIotHub,3000)

            }
            override fun onFailure(e: Exception?) {

            }
        })

    }




}