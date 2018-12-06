package com.mdp.innovation.obd_driving_api.app.core.service

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import com.mdp.innovation.obd_driving_api.app.core.KalmanLatLong
import android.content.IntentFilter
import android.os.BatteryManager
import android.content.BroadcastReceiver
import android.content.Context
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import android.location.LocationManager
import android.support.v4.content.LocalBroadcastManager
import android.os.SystemClock
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import android.location.Criteria






/**
 * Created by louislopez on 03,December,2018
 * MDP Consulting,
 * Peru, Lima.
 */
class LocationService : Service(), LocationListener{

    val LOG_TAG = javaClass.simpleName
    private val binder = LocationServiceBinder()
    var isLocationManagerUpdatingLocation: Boolean = false

    lateinit var locationList: ArrayList<Location>

    lateinit var oldLocationList: ArrayList<Location>
    lateinit var noAccuracyLocationList: ArrayList<Location>
    lateinit var inaccurateLocationList: ArrayList<Location>
    lateinit var kalmanNGLocationList: ArrayList<Location>

    var isLogging: Boolean = false
    var currentSpeed = 0.0f // meters/second
    lateinit var kalmanFilter: KalmanLatLong
    var runStartTimeInMillis: Long = 0

    lateinit var batteryLevelArray: ArrayList<Int>
    lateinit var batteryLevelScaledArray: ArrayList<Float>
    var batteryScale: Int = 0
    var gpsCount: Int = 0

    override fun onCreate() {
        isLocationManagerUpdatingLocation = false
        locationList = ArrayList()
        noAccuracyLocationList = ArrayList()
        oldLocationList = ArrayList()
        inaccurateLocationList = ArrayList()
        kalmanNGLocationList = ArrayList()
        kalmanFilter = KalmanLatLong(3.toFloat())
        //kalmanFilter.KalmanLatLong()

        isLogging = false

        batteryLevelArray = ArrayList()
        batteryLevelScaledArray = ArrayList()
        registerReceiver(this.batteryInfoReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        //super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return Service.START_STICKY
    }
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    override fun onRebind(intent: Intent) {
        LogUtils().v(LOG_TAG, "onRebind ")
    }

    override fun onUnbind(intent: Intent): Boolean {
        LogUtils().v(LOG_TAG, "onUnbind ")
        return true
    }
    override fun onDestroy() {
        LogUtils().v(LOG_TAG, "onDestroy ")
    }

    //Aquí es donde detectamos que la aplicación está siendo eliminada, por lo tanto detén el servicio.
    override fun onTaskRemoved(rootIntent: Intent) {
        LogUtils().v(LOG_TAG, "onTaskRemoved ")
        this.stopUpdatingLocation()
        stopSelf()
    }

    /**
     * Binder class
     *
     * @author Luis Lopez
     */
    inner class LocationServiceBinder : Binder() {
        val service: LocationService
            get() = this@LocationService
    }

    /* LocationListener implemenation */
    override fun onLocationChanged(newLocation: Location) {
        LogUtils().v(LOG_TAG, "(" + newLocation.latitude + "," + newLocation.longitude + ")")
        gpsCount++
        if (isLogging) {
            //locationList.add(newLocation);
            filterAndAddLocation(newLocation)
        }
        val intent = Intent("LocationUpdated")
        intent.putExtra("location", newLocation)
        LocalBroadcastManager.getInstance(this.application).sendBroadcast(intent)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(true);
        }
    }

    override fun onProviderDisabled(provider: String) {
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            notifyLocationProviderStatusUpdated(false)
        }
    }

    private fun getLocationAge(newLocation: Location): Long {
        val locationAge: Long
        locationAge = if (android.os.Build.VERSION.SDK_INT >= 17) {
            val currentTimeInMilli = SystemClock.elapsedRealtimeNanos() / 1000000
            val locationTimeInMilli = newLocation.elapsedRealtimeNanos / 1000000
            currentTimeInMilli - locationTimeInMilli
        } else {
            System.currentTimeMillis() - newLocation.time
        }
        return locationAge
    }

    private fun filterAndAddLocation(location: Location): Boolean {
        val age = getLocationAge(location)

        if (age > 5 * 1000) { //more than 5 seconds
            LogUtils().v(LOG_TAG, "Location is old")
            oldLocationList.add(location)
            return false
        }
        if (location.accuracy <= 0) {
            LogUtils().v(LOG_TAG, "Latitidue and longitude values are invalid.")
            noAccuracyLocationList.add(location)
            return false
        }
        //setAccuracy(newLocation.getAccuracy());
        val horizontalAccuracy = location.accuracy
        if (horizontalAccuracy > 10) { //10meter filter
            LogUtils().v(LOG_TAG, "Accuracy is too low.")
            inaccurateLocationList.add(location)
            return false
        }
        /* Kalman Filter */
        val Qvalue: Float

        val locationTimeInMillis = location.elapsedRealtimeNanos / 1000000
        val elapsedTimeInMillis = locationTimeInMillis - runStartTimeInMillis

        if (currentSpeed === 0.0f) {
            Qvalue = 3.0f //3 meters per second
        } else {
            Qvalue = currentSpeed // meters per second
        }

        kalmanFilter.Process(location.latitude, location.longitude, location.accuracy, elapsedTimeInMillis, Qvalue)
        val predictedLat = kalmanFilter.get_lat()
        val predictedLng = kalmanFilter.get_lng()

        val predictedLocation = Location("")//provider name is unecessary
        predictedLocation.latitude = predictedLat//your coords of course
        predictedLocation.longitude = predictedLng
        val predictedDeltaInMeters = predictedLocation.distanceTo(location)

        if (predictedDeltaInMeters > 60) {
            LogUtils().v(LOG_TAG, "Kalman Filter detects mal GPS, we should probably remove this from track")
            kalmanFilter.consecutiveRejectCount += 1
            if (kalmanFilter.consecutiveRejectCount > 3) {
                kalmanFilter = KalmanLatLong(3.toFloat()) //reset Kalman Filter if it rejects more than 3 times in raw.
                //kalmanFilter.KalmanLatLong(3.toFloat())
            }
            kalmanNGLocationList.add(location)
            return false
        } else {
            kalmanFilter.consecutiveRejectCount = 0
        }
        /* Notifiy predicted location to UI */
        val intent = Intent("PredictLocation")
        intent.putExtra("location", predictedLocation)
        LocalBroadcastManager.getInstance(this.application).sendBroadcast(intent)

        LogUtils().v(LOG_TAG, "Location quality is good enough.")
        currentSpeed = location.speed
        locationList.add(location)

        return true
    }

    private fun notifyLocationProviderStatusUpdated(isLocationProviderAvailable: Boolean) {
        //Broadcast cambio de estado del proveedor de ubicación aquí
    }


    fun startLogging() {
        isLogging = true
    }

    fun stopLogging() {
        if (locationList.size > 1 && batteryLevelArray.size > 1) {
            val currentTimeInMillis = SystemClock.elapsedRealtimeNanos() / 1000000
            val elapsedTimeInSeconds = (currentTimeInMillis - runStartTimeInMillis) / 1000
            var totalDistanceInMeters = 0f
            for (i in 0 until locationList.size - 1) {
                totalDistanceInMeters += locationList[i].distanceTo(locationList[i + 1])
            }
            val batteryLevelStart = batteryLevelArray[0]
            val batteryLevelEnd = batteryLevelArray[batteryLevelArray.size - 1]

            val batteryLevelScaledStart = batteryLevelScaledArray[0].toFloat()
            val batteryLevelScaledEnd = batteryLevelScaledArray[batteryLevelScaledArray.size - 1]

            saveLog(
                elapsedTimeInSeconds,
                totalDistanceInMeters.toDouble(),
                gpsCount,
                batteryLevelStart,
                batteryLevelEnd,
                batteryLevelScaledStart,
                batteryLevelScaledEnd
            )
        }
        isLogging = false
    }

    fun startUpdatingLocation() {
        if (this.isLocationManagerUpdatingLocation === false) {
            isLocationManagerUpdatingLocation = true
            runStartTimeInMillis = SystemClock.elapsedRealtimeNanos() / 1000000


            locationList.clear()

            oldLocationList.clear()
            noAccuracyLocationList.clear()
            inaccurateLocationList.clear()
            kalmanNGLocationList.clear()

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            //Exception thrown when GPS or Network provider were not available on the user's device.
            try {
                val criteria = Criteria()
                criteria.accuracy =
                        Criteria.ACCURACY_FINE //setAccuracyは内部では、https://stackoverflow.com/a/17874592/1709287の用にHorizontalAccuracyの設定に変換されている。
                criteria.powerRequirement = Criteria.POWER_HIGH
                criteria.isAltitudeRequired = false
                criteria.isSpeedRequired = true
                criteria.isCostAllowed = true
                criteria.isBearingRequired = false

                //API level 9 and up
                criteria.horizontalAccuracy = Criteria.ACCURACY_HIGH
                criteria.verticalAccuracy = Criteria.ACCURACY_HIGH
                //criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
                //criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);

                val gpsFreqInMillis = 1000
                val gpsFreqInDistance = 1  // in meters

                //locationManager.addGpsStatusListener(this)

                locationManager.requestLocationUpdates(
                    gpsFreqInMillis.toLong(),
                    gpsFreqInDistance.toFloat(),
                    criteria,
                    this,
                    null
                )

                /* Battery Consumption Measurement */
                gpsCount = 0
                batteryLevelArray.clear()
                batteryLevelScaledArray.clear()

            } catch (e: IllegalArgumentException) {
                LogUtils().v(LOG_TAG, e.localizedMessage)
            } catch (e: SecurityException) {
                LogUtils().v(LOG_TAG, e.localizedMessage)
            } catch (e: RuntimeException) {
                LogUtils().v(LOG_TAG, e.localizedMessage)
            }

        }
    }

    fun stopUpdatingLocation() {
        if (this.isLocationManagerUpdatingLocation === true) {
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.removeUpdates(this)
            isLocationManagerUpdatingLocation = false
        }
    }

    /* Battery Consumption */
    private val batteryInfoReceiver = object : BroadcastReceiver() {
        override  fun onReceive(ctxt: Context, intent: Intent) {
            val batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            val batteryLevelScaled = batteryLevel / scale.toFloat()

            batteryLevelArray.add(Integer.valueOf(batteryLevel))
            batteryLevelScaledArray.add(java.lang.Float.valueOf(batteryLevelScaled))
            batteryScale = scale
        }
    }

    /* Data Logging */
    @Synchronized
    fun saveLog(
        timeInSeconds: Long,
        distanceInMeters: Double,
        gpsCount: Int,
        batteryLevelStart: Int,
        batteryLevelEnd: Int,
        batteryLevelScaledStart: Float,
        batteryLevelScaledEnd: Float
    ) {
        val fileNameDateTimeFormat = SimpleDateFormat("yyyy_MMdd_HHmm")
        val filePath = (this.getExternalFilesDir(null)!!.absolutePath + "/"
                + fileNameDateTimeFormat.format(Date()) + "_battery" + ".csv")

        LogUtils().v(LOG_TAG, "saving to $filePath")

        var fileWriter: FileWriter? = null
        try {
            fileWriter = FileWriter(filePath, false)
            fileWriter!!.append("Time,Distance,GPSCount,BatteryLevelStart,BatteryLevelEnd,BatteryLevelStart(/$batteryScale),BatteryLevelEnd(/$batteryScale)\n")
            val record =
                "" + timeInSeconds + ','.toString() + distanceInMeters + ','.toString() + gpsCount + ','.toString() + batteryLevelStart + ','.toString() + batteryLevelEnd + ','.toString() + batteryLevelScaledStart + ','.toString() + batteryLevelScaledEnd + '\n'.toString()
            fileWriter!!.append(record)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter!!.close()
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                }

            }
        }
    }


}
