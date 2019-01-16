package com.mdp.innovation.obd_driving_api.app.core.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.app.utils.UtilsLocationService

/**
 * Created by louislopez on 05,December,2018
 * MDP Consulting,
 * Peru, Lima.
 */
class LocationUpdatesService : Service() {
    val PACKAGE_NAME = "com.mdp.innovation.obd_driving_api.app.core.service"
    val TAG = javaClass.simpleName

    private val mBinder = LocalBinder()

    /**
     * The name of the channel for notifications.
     */
    private val CHANNEL_ID = "channel_001"
    internal val ACTION_BROADCAST = "${PACKAGE_NAME} .broadcast"
    val EXTRA_LOCATION = "${PACKAGE_NAME} .location"
    private val EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME.toString() + ".started_from_notification"


    /**
     * El intervalo deseado para las actualizaciones de ubicación Inexacto.
     * Las actualizaciones pueden ser más o menos frecuentes.
     */
    private val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 1000

    /**
     * La tasa más rápida para actualizaciones de ubicación activa.
     * Las actualizaciones nunca serán más frecuentes que este valor.
     */
    private val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    /**
     * El identificador de la notificación que se muestra para el servicio en primer plano.
     */
    private val NOTIFICATION_ID = 12345678

    lateinit var mNotificationManager: NotificationManager

    private var mChangingConfiguration = false


    /**
     * Contains parameters used by [com.google.android.gms.location.FusedLocationProviderApi].
     */
    private lateinit var mLocationRequest: LocationRequest

    lateinit var mFusedLocationClient: FusedLocationProviderClient

    /**
     * Devolución de llamada para cambios en la ubicación.
     */
    lateinit var mLocationCallback: LocationCallback

    /**
     * La ubicacion actual.
     */
    lateinit var mLocation: Location

    private var mServiceHandler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult!!.lastLocation)
            }
        }
        createLocationRequest()
        getLastLocation()

        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requiere un canal de notificación.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)
        }

    }

    fun RemoveAll(){
        removeLocationUpdates()
        //stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        LogUtils().v(TAG, " Service started")
        val startedFromNotification = intent.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        )
        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }


    /**
     * Se llama cuando un cliente ()
     * llega al primer plano y se enlaza con este servicio.
     * El servicio debe dejar de ser un servicio de primer plano cuando eso sucede.
     */
    override fun onBind(p0: Intent): IBinder {
        LogUtils().v(TAG, " in onBind()")
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }

    /**
     * Se llama cuando un cliente ()
     * vuelve al primer plano y se enlaza nuevamente con este servicio.
     * El servicio debe dejar de ser un servicio de primer plano cuando eso sucede.
     */
    override fun onRebind(intent: Intent) {
        LogUtils().v(TAG, " in onRebind()")
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    /**
     * Se llama cuando el último cliente () se desvincula de este servicio.
     * Si este método se llama debido a un cambio de configuración en MainActivity,
     * no hacemos nada. De lo contrario, hacemos de este servicio un servicio de primer plano.
     */
    override fun onUnbind(intent: Intent): Boolean {
        //startForeground(NOTIFICATION_ID, getNotification())
        if (!mChangingConfiguration && UtilsLocationService().requestingLocationUpdates(this)) {
            LogUtils().v(TAG, " Starting foreground service")
            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true
    }


    override fun onDestroy() {
        LogUtils().v(TAG, "onDestroy ")
        mServiceHandler!!.removeCallbacksAndMessages(null)
    }

    fun requestLocationUpdates() {
        LogUtils().v(TAG, " Requesting location updates")
        UtilsLocationService().setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, LocationUpdatesService::class.java))
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            UtilsLocationService().setRequestingLocationUpdates(this, false)
            LogUtils().v(TAG, " Lost location permission. Could not request updates. $unlikely")
        }

    }


    fun removeLocationUpdates() {
        LogUtils().v(TAG, "Removing location updates")
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback)
            UtilsLocationService().setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            UtilsLocationService().setRequestingLocationUpdates(this, true)
            LogUtils().v(TAG, "Lost location permission. Could not remove updates. $unlikely")
        }

    }

    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private fun getNotification(): Notification {
        val intent = Intent(this, LocationUpdatesService::class.java)

        val text = UtilsLocationService().getLocationText(mLocation)

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent = PendingIntent.getService(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // The PendingIntent to launch activity.
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, PairObdActivity::class.java), 0
        )

        val builder = NotificationCompat.Builder(this)
            .addAction(
                R.drawable.ic_bluetooth, getString(R.string.title_pref_bluetooth),
                activityPendingIntent
            )
            .addAction(
                R.drawable.ic_bluetooth, getString(R.string.remove_location_updates),
                servicePendingIntent
            )
            .setContentText(text)
            .setContentTitle(UtilsLocationService().getDateToDay())
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_bluetooth)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID) // Channel ID
        }

        return builder.build()
    }




    private fun getLastLocation() {
        try {
            mFusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        mLocation = task.result!!
                    } else {
                        LogUtils().v(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            LogUtils().v(TAG, "Lost location permission.$unlikely")
        }

    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = 1000
        mLocationRequest.fastestInterval = 1000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    fun onNewLocation(location: Location) {
        LogUtils().v(TAG, "New location: ${UtilsLocationService().getLocationText(location)}")
        ConnectOBD.stateUpdateLocation(location = location)
        mLocation = location
        // Notify anyone listening for broadcasts about the new location.
    /*    val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)*/

        // Update notification content if running as a foreground service.
        //mNotificationManager.notify(NOTIFICATION_ID, getNotification())
      if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification())
            LogUtils().v(" MainAct - back", UtilsLocationService().getLocationText(location))
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The [Context].
     */
    fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager
        for (service in manager.getRunningServices(
            Integer.MAX_VALUE
        )) {
            LogUtils().v(TAG, " 1 ${javaClass.name}")
            LogUtils().v(TAG, " 2 ${service.service.className}.")
            if (javaClass.name == service.service.className) {
                LogUtils().v(TAG, " 3 ${service.foreground}.")
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    inner class LocalBinder : Binder() {
        val service: LocationUpdatesService
            get() = this@LocationUpdatesService
    }

}