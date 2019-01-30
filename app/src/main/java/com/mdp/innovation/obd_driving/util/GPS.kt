package com.mdp.innovation.obd_driving.util

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class GPS {

    interface OnConnectionFinishedListener {
        fun onGPSConnection(code: Int)
        fun onStartResolution(status: Status)
    }



    companion object {

        const val ERROR = 0
        const val OK = 1
        const val NO_ENABLED = 2
        const val NO_SUPPORTED = 3
        const val START_RESOLUTION = 4

        private var googleApiClient: GoogleApiClient? = null
        const val REQUEST_LOCATION = 1999

        fun validate(activity: Activity, listener: OnConnectionFinishedListener){

            val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if(!hasGPSDevice(activity)){
                //Gps not Supported
                listener.onGPSConnection(NO_SUPPORTED)
            }else if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //Gps already enabled"
                listener.onGPSConnection(OK)
            }else{
                //GPS no enabled
                listener.onGPSConnection(NO_ENABLED)

                if (googleApiClient == null) {
                    googleApiClient = GoogleApiClient.Builder(activity)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(object: GoogleApiClient.ConnectionCallbacks {
                            override fun onConnected(bundle: Bundle?) {
                                //listener.onGPSConnection(OK)
                            }

                            override fun onConnectionSuspended(i: Int) {
                                googleApiClient!!.connect()
                            }
                        })
                        .addOnConnectionFailedListener(object: GoogleApiClient.OnConnectionFailedListener {
                            override fun onConnectionFailed(connectionResult: ConnectionResult) {
                                Log.d("Location error","Location error " + connectionResult.errorCode)
                                //listener.onGPSConnection(ERROR)
                            }
                        }).build()
                    googleApiClient!!.connect()
                }

                val locationRequest = LocationRequest.create()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 30 * 1000
                locationRequest.fastestInterval = 5 * 1000
                val builder: LocationSettingsRequest.Builder  = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

                builder.setAlwaysShow(true)

                val result: PendingResult<LocationSettingsResult> =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
                result.setResultCallback(object: ResultCallback<LocationSettingsResult> {
                    override fun onResult(result: LocationSettingsResult) {
                        val status = result.status
                        when(status.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().

                                    //status.startResolutionForResult(activity, REQUEST_LOCATION)
                                    listener.onStartResolution(status)
                                    //activity!!.finish()
                                } catch (e: IntentSender.SendIntentException) {
                                    // Ignore the error.
                                }
                            }
                            LocationSettingsStatusCodes.SUCCESS -> {
                                listener.onGPSConnection(OK)
                            }
                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE ->{
                                listener.onGPSConnection(NO_SUPPORTED)
                            }

                        }
                    }
                })


            }



        }

        private fun hasGPSDevice(context: Context): Boolean {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (manager == null) return false

            val providers = manager.allProviders
            if (providers == null) return false
            return providers.contains(LocationManager.GPS_PROVIDER)
        }

    }




}