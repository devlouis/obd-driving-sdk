package com.mdp.innovation.obd_driving_api.data.store

import android.content.Context
import android.content.SharedPreferences

class SharedPreference {
    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var context: Context? = null

    var PRIVATE_MODE = 0
    var PREF_NAME = "SharedPreference"
    var MAC_DEVICE = "SharedPreference"

    constructor(context: Context?) {
        this.context = context
        pref = this.context!!.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = pref!!.edit()
    }

    fun saveMacBluetooth(remoteDevice: String){
        editor!!.putString(MAC_DEVICE, remoteDevice)
        editor!!.commit()
    }

    fun getMacBluetooth(): HashMap<String, String>{
        val macdevice = java.util.HashMap<String, String>()
        macdevice[MAC_DEVICE] = pref!!.getString(MAC_DEVICE, "")
        return macdevice
    }

}