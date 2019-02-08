package com.mdp.innovation.obd_driving_api_v2.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.*
import android.support.annotation.Nullable
import android.support.v4.app.NotificationCompat
import android.support.v4.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD

import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.app.utils.UtilsLocationService
import com.mdp.innovation.obd_driving_api_v2.constants.utils.L


class LocationIntentService: IntentService("LocationIntentService") {
    val PACKAGE_NAME = "com.mdp.innovation.obd_driving_api_v2.intentService2"
    val TAG = javaClass.simpleName

    private val mBinder = LocalBinder()
    /**
     * The name of the channel for notifications.
     */
    private val CHANNEL_ID = "channel_002"
    internal val ACTION_BROADCAST = "${PACKAGE_NAME} .broadcast"
    val EXTRA_LOCATION = "${PACKAGE_NAME} .location"
    private val EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME.toString() + ".started_from_notification2"


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
    private val NOTIFICATION_ID = 123456789

    lateinit var mNotificationManager: NotificationManager

    private var mChangingConfiguration = false


    /**
     * Contains parameters used by [com.google.android.gms.location.FusedLocationProviderApi].
     */
    private lateinit var mLocationRequest: LocationRequest


    var mFusedLocationClient: FusedLocationProviderClient? = null

    @Volatile
    var shouldContinueLoca = true

    /**
     * Devolución de llamada para cambios en la ubicación.
     */
    lateinit var mLocationCallback: LocationCallback

    /**
     * La ubicacion actual.
     */
    lateinit var mLocation: Location

    //private var mServiceHandler: Handler? = null

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onHandleIntent(@Nullable intent: Intent) {
        L.i("onHandleIntent-local" + "Thread is :: " + Thread.currentThread().id)
        L.i("shouldContinueLoca $shouldContinueLoca")

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationRequest()
        getLastLocation()


/*        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)*/
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                L.i("shouldContinueLoca___ $shouldContinueLoca")
                onNewLocation(locationResult!!.lastLocation)
            }
        }


        requestLocationUpdates()
        L.i("onHandleIntent-local bottom")
    }

    override fun onCreate() {
        super.onCreate()

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


    fun onNewLocation(location: Location) {
        LogUtils().v(TAG, "New location: ${UtilsLocationService().getLocationText(location)}")
        //ConnectOBD.stateUpdateLocation(location = location)
        mLocation = location
        // Notify anyone listening for broadcasts about the new location.
        val intent = Intent(ACTION_BROADCAST)
        intent.putExtra(EXTRA_LOCATION, location)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

        // Update notification content if running as a foreground intentService.
        //mNotificationManager.notify(NOTIFICATION_ID, getNotification())
        //if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification())
            LogUtils().v(" MainAct - back", UtilsLocationService().getLocationText(location))
        //}
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
     * Returns the [NotificationCompat] used as part of the foreground intentService.
     */
    private fun getNotification(): Notification {
        val intent = Intent(this, LocationIntentService::class.java)

        val text = UtilsLocationService().getLocationText(mLocation)

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)

        // The PendingIntent that leads to a call to onStartCommand() in this intentService.
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



    override fun onDestroy() {
        LogUtils().v(TAG, "onDestroy ")
        L.i(" Location_intentService onDestroy")
        //mServiceHandler!!.removeCallbacksAndMessages(null)
    }

    /**
     * Returns true if this is a foreground intentService.
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
        val intentService: LocationIntentService
            get() = this@LocationIntentService
    }
}