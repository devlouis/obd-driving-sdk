package com.mdp.innovation.obd_driving_api.app.core

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils

/**
 * Created by louislopez on 18,January,2019
 * MDP Consulting,
 * Peru, Lima.
 */
class UtilsNetwork {
    val TAG = javaClass.simpleName

    private var isWifiConn: Boolean = false
    private var isMobileConn: Boolean = false

    fun isOnline(context: Context){
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            connMgr.allNetworks.forEach { network ->
                connMgr.getNetworkInfo(network).apply {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        isWifiConn = isWifiConn or isConnected
                    }
                    if (type == ConnectivityManager.TYPE_MOBILE) {
                        isMobileConn = isMobileConn or isConnected
                    }
                }
            }
        }

        LogUtils().v(TAG, "Wifi connected: $isWifiConn")
        LogUtils().v(TAG, "Mobile connected: $isMobileConn")
    }

}