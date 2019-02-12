/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mdp.innovation.obd_driving_api_v2.service

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.location.*

import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD.sendDataIoTHub
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.app.utils.UtilsLocationService
import com.mdp.innovation.obd_driving_api_v2.application.ObdPreferences
import com.mdp.innovation.obd_driving_api_v2.constants.DefineObdReader
import com.mdp.innovation.obd_driving_api.enums.ObdProtocols
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.ObdConfig
import com.mdp.innovation.obd_driving_api.commands.control.TroubleCodesCommand
import com.mdp.innovation.obd_driving_api.commands.protocol.*
import com.mdp.innovation.obd_driving_api.data.entity.LocationEntity
import com.mdp.innovation.obd_driving_api.data.entity.ObdEntity
import com.mdp.innovation.obd_driving_api.data.store.repository.LocationRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.ObdRepository
import com.mdp.innovation.obd_driving_api_v2.constants.DefineObdReader.INTENT_OBD_EXTRA_DATA
import com.mdp.innovation.obd_driving_api_v2.constants.DefineObdReader.TAG_BD
import com.mdp.innovation.obd_driving_api_v2.constants.utils.L
import com.mdp.innovation.obd_driving_api_v2.trip.TripRecord


import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


/**
 * created by sohrab 01/12/2017
 *
 *
 * Service for managing connection and data communication with a OBD-2 in background and update data to RealTime screen.
 * It connects paired OBD-2 Or wait until paired.
 * Once it is paired, try to connect with Bluetooth Socket along with some specific OBD-2 command,
 * if connected, fetch data until OBD-2 disconnected and if somehow,
 * it disconnected then go to connect and this is in loop until user quit from App.
 */

class ObdReaderService : IntentService("ObdReaderService"), DefineObdReader {
    // this is used to find TroubleCode if true. This is used in InspectionActivity where fault is shown.
    var mIsFaultCodeRead = true
    private val mBinder = LocalBinder()
    private var mLastNotificationType: Int = 0
    // name of OBD
    private val OBD_SMALL = "obd"
    private val OBD_CAPS = "OBD"
    private val V_LINK = "V-LINK"
    private var mBluetoothManager: BluetoothManager? = null//Bluetooth Manager
    private var mBluetoothAdapter: BluetoothAdapter? = null//Bluetooth adapter
    private var mSocket: BluetoothSocket? = null
    private var mNotificationBuilder: NotificationCompat.Builder? = null
    private var mNotificationManager: NotificationManager? = null
    private var mIsRunningSuccess: Boolean = false
    private var mIntent: Intent? = Intent(DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA)
    private val mSupportedPids: CharArray? = null


    /**
     * Variables obtener Location
     * @param mFusedLocationClient
     * @param mLocationRequest
     */
    var mFusedLocationClient: FusedLocationProviderClient? = null
    lateinit var mLocationRequest: LocationRequest

    // display foreground intentService notification.
    /**
     * El identificador de la notificación que se muestra para el servicio en primer plano.
     */
    private val NOTIFICATION_ID = 12345678
    private val CHANNEL_ID = "channel_001"
    private val PACKAGE_NAME = "com.mdp.innovation.obd_driving_api_v2.intentService"
    private val EXTRA_STARTED_FROM_NOTIFICATION = "$PACKAGE_NAME.started_from_notification"

    /**
     * Notification location
     */

    private val NOTIFICATION_ID_2 = 123456789
    private val CHANNEL_ID_2 = "channel_002"


    /**
     * Valores del viaje
     */
    var VIN = ""
    var statusTrip = "0"
    var contadorTotal = 0


    /**
     * check whether Bluetooth is enable or not
     *
     * @return
     */
    val isEnable: Boolean
        get() = if (mBluetoothAdapter == null) false else mBluetoothAdapter!!.isEnabled

    init {
        L.i("ObdReaderService")
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }


    override fun onHandleIntent(intent: Intent?) {
        L.i("onHandleIntent" + "Thread is :: " + Thread.currentThread().id)
        shouldContinue = true

        setUpAsForeground()
        if (initiateConnection()) {

            if (!isEnable) {
                enableBlutooth()
            }

            findObdDevicesAndConnect()
        }

        L.i("onHandleIntent bottom")
        //  mNotificationManager.cancel(NOTIFICATION_ID);
        if (shouldContinue) {
            ObdPreferences.get(applicationContext).serviceRunningStatus = true
            ObdPreferences.get(applicationContext).isOBDconnected = true
        } else {
            ObdPreferences.get(applicationContext).serviceRunningStatus = false
            ObdPreferences.get(applicationContext).isOBDconnected = false
            TripRecord.getTripRecode(this).clear()
        }

        /*  ObdPreferences.get(getApplicationContext()).setServiceRunningStatus(false);
        ObdPreferences.get(getApplicationContext()).setIsOBDconnected(false);
        TripRecord.getTripRecode(this).clear();*/

    }

    /**
     * This method is recursively called until intentService stopped.
     */
    private fun findObdDevicesAndConnect() {
        L.i(" shouldContinue::**** $shouldContinue")
        if (shouldContinue) {
            if (!isConnected) {
                findPairedDevices()
            }
        } else {
            removeLocationUpdates()
            L.i("removeLocationUpdates()")
            ObdPreferences.get(applicationContext).serviceRunningStatus = false
        }

        if (isConnected) {
            executeCommand()
        }

        if (ObdPreferences.get(applicationContext).serviceRunningStatus) {
            L.i("findObdDevicesAndConnect()")
            findObdDevicesAndConnect()
        }

    }

    /**
     * find paired OBD-2 devices in loop until found and connected or intentService stopped.
     */
    private fun findPairedDevices() {

        while (!isConnected && ObdPreferences.get(applicationContext).serviceRunningStatus) {
            if (mBluetoothAdapter != null) {
                var deviceFound = false

                val bluetoothDevices = mBluetoothAdapter!!.bondedDevices
                for (device in bluetoothDevices) {
                    if (device != null) {
                        val name = device.name
                        if (name != null && (name.contains(OBD_SMALL) || name.contains(OBD_CAPS) || name.toUpperCase().contains(
                                V_LINK
                            ))
                        ) {
                            try {
                                L.i(" :connectOBDDevice: ")
                                connectOBDDevice(device)
                            } catch (e: Exception) {
                                L.i(if ("connectOBDDevice return Exception :: $e" != null) e.message else "")
                            }

                            deviceFound = true
                            break
                        }
                    }

                }

                if (!deviceFound) {
                    /*  if (mLastNotificationType != DEVICE_NOT_PAIRED) {
                        mLastNotificationType = DEVICE_NOT_PAIRED;
                        updateNotification(getString(R.string.waiting_for_obd));
                    }*/
                    sendBroadcast(DefineObdReader.ACTION_OBD_CONNECTION_STATUS, getString(R.string.waiting_for_obd))
                }
            }
        }
    }

    /**
     * connects specified bluetooth OBD device with Bluetooth Socket.
     * if bluetooth socked connected then use some init OBD-2 command to initialize,
     * if command response is success, then we assume connection is established and ready to fetch data.
     *
     * @param device
     * @throws Exception
     */
    @Throws(Exception::class)
    fun connectOBDDevice(device: BluetoothDevice) {

        try {
            mSocket = device.javaClass.getMethod(
                "createInsecureRfcommSocket",
                *arrayOf<Class<*>>(Int::class.javaPrimitiveType!!)
            ).invoke(device, 1) as BluetoothSocket
        } catch (e: Exception) {
            // e.printStackTrace();
            L.i("createInsecureRfcommSocket failed")
            closeSocket()
        }

        if (mSocket != null) {
            try {
                mBluetoothAdapter!!.cancelDiscovery()
                Thread.sleep(500)
                mSocket!!.connect()
                L.i("Socket connected")
            } catch (e: Exception) {
                L.i("Socket connection  exception :: " + e.message)
                //   e.printStackTrace();
                closeSocket()
            }

            var isSockedConnected = mSocket!!.isConnected
            if (isSockedConnected) {
                try {
                    Thread.sleep(DELAY_TWO_SECOND.toLong())
                    if (mLastNotificationType != INIT_OBD) {
                        mLastNotificationType = INIT_OBD
                        updateNotification(getString(R.string.connecting_to_ecu))
                    }
                    L.i("Executing reset command in new Thread :: " + Thread.currentThread().id)
                    val newThread = Thread(Runnable {
                        try {

                            // this thread is required because in Headunit command.run method block infinitly ,
                            // therefore this thread life is maximum 15 second so that block can be handled.
                            mIsRunningSuccess = false
                            ObdResetCommand().run(mSocket!!.inputStream, mSocket!!.outputStream)
                            Thread.sleep(1000)
                            EchoOffCommand().run(mSocket!!.inputStream, mSocket!!.outputStream)
                            Thread.sleep(200)
                            LineFeedOffCommand().run(mSocket!!.inputStream, mSocket!!.outputStream)
                            Thread.sleep(200)
                            SpacesOffCommand().run(mSocket!!.inputStream, mSocket!!.outputStream)
                            Thread.sleep(200)
                            SpacesOffCommand().run(mSocket!!.inputStream, mSocket!!.outputStream)
                            Thread.sleep(200)
                            TimeoutCommand(125).run(mSocket!!.inputStream, mSocket!!.outputStream)
                            //  updateNotification(getString(R.string.searching_protocol));
                            Thread.sleep(200)
                            SelectProtocolCommand(ObdProtocols.AUTO).run(mSocket!!.inputStream, mSocket!!.outputStream)
                            Thread.sleep(200)
                            EchoOffCommand().run(mSocket!!.inputStream, mSocket!!.outputStream)
                            //  updateNotification(getString(R.string.searching_supported_sensor));
                            Thread.sleep(200)
                            mIsRunningSuccess = true
                            // checkPid0To20(true);

                        } catch (e: Exception) {
                            mIsRunningSuccess = false
                            L.i(if ("In new thread reset command  exception :: $e" != null) e.message else "")
                        }
                    })

                    newThread.start()
                    newThread.join(DELAY_FIFTEEN_SECOND.toLong())
                    L.i("Thread wake to check reset command status  i.e  :: " + Thread.currentThread().id + ",  mIsRunningSuccess :: " + mIsRunningSuccess)
                    isSockedConnected = mIsRunningSuccess

                } catch (e: Exception) {
                    L.i(" reset command Exception  :: " + e.message)
                    isSockedConnected = false
                }

            }

            if (mSocket != null && mSocket!!.isConnected && isSockedConnected) {
                setConnection(false)
                if (mLastNotificationType != OBD_CONNECTED) {
                    mLastNotificationType = OBD_CONNECTED
                    updateNotification(getString(R.string.obd_connected))
                }
            } else {
                if (mSupportedPids != null && mSupportedPids.size == 32) {

                    if (mSupportedPids[12] != PID_STATUS_SUCCESS || mSupportedPids[11] != PID_STATUS_SUCCESS) {
                        // speed pid not supportedsupported
                        // updateNotification(getString(R.string.unable_to_connect));
                        sendBroadcast(
                            DefineObdReader.ACTION_OBD_CONNECTION_STATUS,
                            getString(R.string.unable_to_connect)
                        )
                        return
                    }
                }

                sendBroadcast(
                    DefineObdReader.ACTION_OBD_CONNECTION_STATUS,
                    getString(R.string.obd2_adapter_not_responding)
                )
                /*
                if (mLastNotificationType != OBD_NOT_RESPONDING) {
                    mLastNotificationType = OBD_NOT_RESPONDING;
                    updateNotification(getString(R.string.obd2_adapter_not_responding));
                }
*/
            }
        }

    }


    /**
     * Once OBD-2 connected, this method will execute to fetch data continuously until OBD disconnected or Service stopped.
     */
    private fun executeCommand() {
        L.i("executing commands thread is :: " + Thread.currentThread().id)
        val tripRecord = TripRecord.getTripRecode(this)
        val commands = ObdConfig.getCommandCustom().clone() as ArrayList<ObdCommand>
        var count = 0

        L.i(" shouldContinue::____ $shouldContinue")
        while (mSocket != null && mSocket!!.isConnected && commands.size > count && isConnected && ObdPreferences.get(
                applicationContext
            ).serviceRunningStatus && shouldContinue
        ) {

            val command = commands[count]
            L.i(" shouldContinue:: $shouldContinue")
            //if (shouldContinue){
            try {
                L.i("command run :: " + command.name)
                command.run(mSocket!!.inputStream, mSocket!!.outputStream)
                L.i("result is :: " + command.formattedResult + " :: name is :: " + command.name)
                LogUtils().v(TAG_BD, " result is :: ${command.formattedResult}:: name is ::${command.name}")

                tripRecord.updateTrip(command.name, command)
                if (mIsFaultCodeRead) {
                    try {
                        val troubleCodesCommand = TroubleCodesCommand()
                        troubleCodesCommand.run(mSocket!!.inputStream, mSocket!!.outputStream)
                        tripRecord.updateTrip(troubleCodesCommand.name, troubleCodesCommand)
                        mIsFaultCodeRead = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

                if (mIntent == null)
                    mIntent = Intent(DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA)
                //sendBroadcast(mIntent);
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(mIntent!!)

                if (command.name === "Vehicle Speed"){
                    contadorTotal++
                    VIN = tripRecord.getmVehicleIdentificationNumber()
                    addToBdObd(tripRecord.getmVehicleIdentificationNumber(), tripRecord.engineRpm, tripRecord.speed.toString(), contadorTotal)
                    Thread.sleep(1000)
                }


            } catch (e: Exception) {
                L.i("execute command Exception  :: " + e.message)

                if (!TextUtils.isEmpty(e.message) && (e.message == "Broken pipe" || e.message == "Connection reset by peer")) {
                    L.i("command Exception  :: " + e.message)
                    setDisconnection()
                    /*
                    if (mLastNotificationType != OBD_NOT_RESPONDING) {
                        mLastNotificationType = OBD_NOT_RESPONDING;
                        updateNotification(getString(R.string.obd2_adapter_not_responding));
                    }
*/
                }
            }

            /*    }else {
                L.i(" SE DETIENE");
                stopSelf();
            }*/


            count++
            if (count == commands.size) {
                count = 0
            }

        }

        // exit loop means connection lost, so set connection status false
        isConnected = false

    }

    /**
     * send broadcast with specific action and data
     *
     * @param action
     * @param data
     */
    private fun sendBroadcast(action: String, data: String) {
        val intent = Intent(action)
        intent.putExtra(INTENT_OBD_EXTRA_DATA, data)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        //sendBroadcast(intent);
    }

    /**
     * send broadcast with specific action
     *
     * @param action
     */
    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
        //sendBroadcast(intent);
    }

    lateinit var mLocation: Location
    lateinit var mLocationCallback: LocationCallback


    override fun onCreate() {
        super.onCreate()
        //fetchLocation();
        ObdPreferences.get(applicationContext).serviceRunningStatus = true
        ObdPreferences.get(applicationContext).isOBDconnected = false
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager!!.createNotificationChannel(mChannel)
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        getLastLocation()

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult!!.lastLocation)
            }
        }

        requestLocationUpdates()
        L.i("Service Created :: ")
    }


    /**
     * Location
     * @param onNEwLocation
     * @param getLastLocation
     * @param createLocationRequest
     * @param requestLocationUpdates
     * @param removeLocationUpdates
     */
    fun onNewLocation(location: Location) {
        LogUtils().v(TAG, "New location: ${UtilsLocationService().getLocationText(location)}")
        //ConnectOBD.stateUpdateLocation(location = location)

        addToBdLocation(location)

        mLocation = location
        // Notify anyone listening for broadcasts about the new location.
       /* val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)*/

        // Update notification content if running as a foreground intentService.
        //mNotificationManager.notify(NOTIFICATION_ID, getNotification())
        //if (serviceIsRunningInForeground(this)) {
        //mNotificationManager.notify(NOTIFICATION_ID, getNotification())
        val text = UtilsLocationService().getLocationText(mLocation)

        mNotificationBuilder!!.setContentText(text)
        mNotificationBuilder!!.setContentTitle(UtilsLocationService().getDateToDay())
        mNotificationManager!!.notify(NOTIFICATION_ID, mNotificationBuilder!!.build())

        LogUtils().v(" MainAct - back", UtilsLocationService().getLocationText(location))
        //}
    }


    private fun getLastLocation() {
        try {
            mFusedLocationClient!!.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        mLocation = task.result!!
                    } else {
                        LogUtils().v("BD_LOCAL", "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            LogUtils().v("BD_LOCAL", "Lost location permission.$unlikely")
        }

    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    fun requestLocationUpdates() {
        LogUtils().v(TAG, " Requesting location updates")
        UtilsLocationService().setRequestingLocationUpdates(this, true)
        //mContext.startService(Intent(applicationContext, LocationIntentService::class.java))
        try {
            mFusedLocationClient!!.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
            //startSensor()
        } catch (unlikely: SecurityException) {
            UtilsLocationService().setRequestingLocationUpdates(this, false)
            LogUtils().v(TAG, " Lost location permission. Could not request updates. $unlikely")
        }

    }



    fun removeLocationUpdates() {
        LogUtils().v(TAG, "Removing location updates")
        try {
            //stopSensor()
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
            UtilsLocationService().setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            UtilsLocationService().setRequestingLocationUpdates(this, true)
            LogUtils().v(TAG, "Lost location permission. Could not remove updates. $unlikely")
        }
    }

    /**
     * check whether this devices support bluetooth
     *
     * @return
     */
    protected fun initiateConnection(): Boolean {
        val isBlueToothSupported = packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
        val isInitialized = initialize()

        if (!isBlueToothSupported || !isInitialized) {
            Toast.makeText(this, getString(R.string.bluetooth_unsupported), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * check BluetoothServices available in this device or not
     *
     * @return
     */
    fun initialize(): Boolean {
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {

                return false
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter = mBluetoothManager!!.adapter
        } else {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        }
        return if (mBluetoothAdapter == null) {
            false
        } else true
    }

    private fun setUpAsForeground() {
        val intent = Intent(this, ObdReaderService::class.java)
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

        val contentIntent = PendingIntent.getService(
            this, 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )


        mNotificationBuilder = NotificationCompat.Builder(this)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.waiting_for_obd))
            .setSmallIcon(R.drawable.ic_bluetooth)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setContentIntent(contentIntent)

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationBuilder!!.setChannelId(CHANNEL_ID) // Channel ID
        }

        startForeground(NOTIFICATION_ID, mNotificationBuilder!!.build())
    }


    /**
     * Updates the notification.
     */
    private fun updateNotification(text: String) {
        mNotificationBuilder!!.setContentText(text)
        mNotificationManager!!.notify(NOTIFICATION_ID, mNotificationBuilder!!.build())
    }

    /**
     * Updates the notification.
     */
    /*
    public void updateNotificationString() {
        String text = "";
        if (mLastNotificationType == OBD_CONNECTED) {
            text = getString(R.string.connected_ok);
        } else if (mLastNotificationType == OBD_NOT_RESPONDING) {
            text = getString(R.string.obd2_adapter_not_responding);
        }
        if (mLastNotificationType == DEVICE_NOT_PAIRED) {
            text = getString(R.string.waiting_for_obd);
        }
        mNotificationBuilder.setContentTitle(getString(R.string.app_name));
        mNotificationBuilder.setContentText(text);
        mNotificationManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
    }
*/
    override fun onDestroy() {
        super.onDestroy()
        L.i("intentService onDestroy")
        shouldContinue = false
        //mNotificationManager.cancel(NOTIFICATION_ID);
        closeSocket()
        ObdPreferences.get(applicationContext).serviceRunningStatus = false
        ObdPreferences.get(applicationContext).isOBDconnected = false
        TripRecord.getTripRecode(this).clear()
    }

    /**
     * close Bluetooth Socket
     */
    private fun closeSocket() {
        L.i("socket closed :: ")
        if (mSocket != null) {
            try {
                mSocket!!.close()
            } catch (e: IOException) {
                L.i("socket closing failed :: ")
            }

        }
    }

    /**
     * enable bluetooth without user interaction
     *
     * @return
     */
    fun enableBlutooth(): Boolean {
        return if (mBluetoothAdapter != null) mBluetoothAdapter!!.enable() else false
    }

    override fun stopService(name: Intent): Boolean {
        ObdPreferences.get(applicationContext).serviceRunningStatus = false
        return super.stopService(name)

    }

    /*Method used to set device disconnected state through the application...*/
    fun setDisconnection() {
        /*
        if (mLastNotificationType != OBD_NOT_RESPONDING) {
            mLastNotificationType = OBD_NOT_RESPONDING;
            updateNotification(getString(R.string.obd2_adapter_not_responding));
        }
*/

        ObdPreferences.get(applicationContext).isOBDconnected = false
        isConnected = false
        closeSocket()
        L.i("socket disconnected :: ")
        //  broadcastUpdate(ACTION_OBD_DISCONNECTED);
        sendBroadcast(DefineObdReader.ACTION_OBD_CONNECTION_STATUS, getString(R.string.connect_lost))
    }

    /*Method used to set device connected state through the application...*/
    private fun setConnection(isFromBle: Boolean) {

        ObdPreferences.get(applicationContext).isOBDconnected = true
        isConnected = true
        // sendBroadcast(ACTION_OBD_CONNECTED, String.valueOf(isFromBle));
        sendBroadcast(DefineObdReader.ACTION_OBD_CONNECTION_STATUS, getString(R.string.obd_connected))
    }

    /**
     * create Binder instance used to return in onBind method
     */
    inner class LocalBinder : Binder() {
        val service: ObdReaderService
            get() = this@ObdReaderService
    }

    companion object {
        private val TAG = "ObdReaderService"
        // receive when OBD-2 connected


        val PID_STATUS_SUCCESS = '1'
        val DEVICE_NOT_PAIRED = 1
        val OBD_NOT_RESPONDING = 2
        val OBD_CONNECTED = 3
        val INIT_OBD = 4


        //   private static final int NOTIFICATION_ID = 101;
        private val DELAY_FIFTEEN_SECOND = 15000
        private val DELAY_TWO_SECOND = 2000
        //set OBD-2 connection status
        var isConnected: Boolean = false

        @Volatile
        var shouldContinue = true
    }


    private fun addToBdObd(vin: String, rpm: String, kmh: String, count: Int) {
        val sdf6 = SimpleDateFormat("H:mm:ss")
        val currentDateandTime = sdf6.format(Date())

        val sdf = SimpleDateFormat("yyyy:MM:dd")
        val currentToDay = sdf.format(Date())


        val obdEntity = ObdEntity()
        obdEntity.userId = "5c460df4387a710934beb1e7"
        obdEntity.id_trip = sendDataIoTHub.getIDTrip(applicationContext, vin)
        obdEntity.vin = vin
        obdEntity.kmh = kmh
        obdEntity.rpm = rpm
        obdEntity.dataNew = currentToDay.replace(":","-") +" "+ currentDateandTime
        obdEntity.status = statusTrip
   /*     if (sensorAccelerometer != null){
            obdEntity.ax = sensorAccelerometer!!.values[0]
            obdEntity.ay = sensorAccelerometer!!.values[1]
            obdEntity.az = sensorAccelerometer!!.values[2]
        }*/

        ObdRepository(Application()).addObd(obdEntity)
        LogUtils().v(TAG_BD, " OBD ADD : ${currentDateandTime} = ${obdEntity.toString()}")
    }

    private fun addToBdLocation(location: Location) {
        val sdf6 = SimpleDateFormat("H:mm:ss")
        val currentDateandTime = sdf6.format(Date())

        val sdf = SimpleDateFormat("yyyy:MM:dd")
        val currentToDay = sdf.format(Date())

        val locationEntity = LocationEntity()
        locationEntity.userId = "5c460df4387a710934beb1e7"
        locationEntity.id_trip = sendDataIoTHub.getIDTrip(applicationContext, VIN)
        locationEntity.longitud = location.longitude.toString()
        locationEntity.latitudd = location.latitude.toString()
        locationEntity.bearing = location.bearing.toString()
        locationEntity.dataNew = currentToDay.replace(":","-") +" "+ currentDateandTime
        locationEntity.status = statusTrip
        LocationRepository(Application()).addLocation(locationEntity)
        LogUtils().v(TAG_BD, " LOCATION ADD: ${currentDateandTime} = ${locationEntity.toString()}")

    }

}
