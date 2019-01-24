package com.mdp.innovation.obd_driving.util

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class Connection {

    interface OnConnectionFinishedListener {
        fun onConnectionFinished(code: Int)
    }

    companion object {

        const val EXCEPTION = 0
        const val OK = 1
        const val NO_NETWORK = 2
        const val NO_CONNECTION = 3

        fun validate(activity: Activity, listener: OnConnectionFinishedListener){

            val connectivityManager: ConnectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo

            if(activeNetworkInfo != null){

                doAsync {

                    try {
                        val urlc: HttpURLConnection = (URL("http://clients3.google.com/generate_204").openConnection()) as HttpURLConnection
                        urlc.setRequestProperty("User-Agent", "Android")
                        urlc.setRequestProperty("Connection", "close")
                        urlc.connectTimeout = 1500
                        urlc.connect()

                        if(urlc.responseCode == 204 && urlc.contentLength == 0){
                            uiThread {
                                Log.d("Connection", "Positivo!!!")
                                listener.onConnectionFinished(OK)
                            }
                        }else{
                            uiThread {
                                Log.d("Connection", "Negativo!!!")
                                listener.onConnectionFinished(NO_CONNECTION)
                            }
                        }





                        /*val p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com")
                        val returnVal: Int = p1.waitFor()
                        val reachable = (returnVal==0)
                        if(reachable){
                            Log.d("Connection", "Positivo!!!")
                            listener.onConnectionFinished(OK)
                        }else{
                            Log.d("Connection", "Negativo!!!")
                            listener.onConnectionFinished(NO_CONNECTION)
                        }*/


                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        uiThread{
                            listener.onConnectionFinished(NO_CONNECTION)
                        }

                    }

                }

            }else{
                Log.d("Connection", "No tiene ninguna red")
                listener.onConnectionFinished(NO_NETWORK)
            }

        }

    }


}