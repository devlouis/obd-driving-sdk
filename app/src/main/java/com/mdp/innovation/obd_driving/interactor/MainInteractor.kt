package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD

class MainInteractor {

    interface OnMainFinishedListener {
        fun onDevicePaired()
        fun onDeviceNoPaired()
    }

    companion object {
        var aux = 0
    }

    fun validateDevicePaired(listener: OnMainFinishedListener) {
        /*var devicePaired = true
        if (devicePaired)  listener.onDevicePaired()
        else listener.onDeviceNoPaired()

        aux += 1*/

        var result = ConnectOBD.verifyMacOBD()
        Log.i("macBluetooth:", result.macBluetooth)
        Log.i("result:", result.result.toString())
        if(result.result){
            listener.onDevicePaired()
        }else{
            listener.onDeviceNoPaired()
        }
    }
}