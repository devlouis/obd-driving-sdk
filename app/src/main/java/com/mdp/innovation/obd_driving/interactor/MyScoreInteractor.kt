package com.mdp.innovation.obd_driving.interactor

import android.bluetooth.BluetoothAdapter



class MyScoreInteractor {

    interface OnMyScoreFinishedListener {
        fun onDeviceConnected()
        fun onDeviceNoConnected()
    }

    fun isConnected(listener: OnMyScoreFinishedListener) {
        var isConnected = true
        if (isConnected)  listener.onDeviceConnected()
        else listener.onDeviceNoConnected()
    }
}