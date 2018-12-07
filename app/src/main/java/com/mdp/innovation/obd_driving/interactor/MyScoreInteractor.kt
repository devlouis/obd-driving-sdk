package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD


class MyScoreInteractor {

    interface OnMyScoreFinishedListener {
        fun onDeviceConnected()
        fun onDeviceNoConnected()
    }

    fun isConnected(listener: OnMyScoreFinishedListener) {
        /*var isConnected = true
        if (isConnected)  listener.onDeviceConnected()
        else listener.onDeviceNoConnected()*/

        var result = ConnectOBD.verifyMacOBD()
        Log.i("macBluetooth:", result.macBluetooth)
        Log.i("result:", result.result.toString())
        if(result.result){
            listener.onDeviceConnected()
        }else{
            listener.onDeviceNoConnected()
        }
    }
}