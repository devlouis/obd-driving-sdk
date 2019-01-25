package com.mdp.innovation.obd_driving_api.data.store

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.GsonBuilder
import com.mdp.innovation.obd_driving_api.app.utils.JSONUtils
import com.mdp.innovation.obd_driving_api.data.entity.FailuresEntity

class SharedPreference {
    var pref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var context: Context? = null

    var PRIVATE_MODE = 0
    var PREF_NAME = "SharedPreference"
    var MAC_DEVICE = "SharedPreference"
    var VIN_CAR = "VIN_CAR"
    var ID_TRIP = "ID_TRIP"
    var ID_RAW_BD = "ID_RAW_BD"
    var FAILURES = "FAILURES"

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

    fun saveVinCar(vin: String){
        editor!!.putString(VIN_CAR, vin)
        editor!!.commit()
    }
    fun getVinCar(): HashMap<String, String>{
        val vin = java.util.HashMap<String, String>()
        vin[VIN_CAR] = pref!!.getString(VIN_CAR, "")
        return vin
    }

    fun saveIdTrip(vin: String){
        editor!!.putString(ID_TRIP, vin)
        editor!!.commit()
    }
    fun getIdTrip(): HashMap<String, String>{
        val vin = java.util.HashMap<String, String>()
        vin[ID_TRIP] = pref!!.getString(ID_TRIP, "")
        return vin
    }

    fun saveIdRawBD(id: String){
        editor!!.putString(ID_RAW_BD, id)
        editor!!.commit()
    }

    fun getIdRawBD(): HashMap<String, String>{
        val id = java.util.HashMap<String, String>()
        id[ID_RAW_BD] = pref!!.getString(ID_RAW_BD, "")
        return id
    }

    fun saveRecordTrip(failuresEntity: FailuresEntity){
        editor!!.putString(FAILURES, JSONUtils.generateJSONObject(failuresEntity).toString())
        editor!!.commit()
    }

    fun getRecordTrip():FailuresEntity {
        var gsonb = GsonBuilder()
        var gson = gsonb.create()
        return  gson.fromJson(pref!!.getString(FAILURES, ""), FailuresEntity::class.java) as FailuresEntity
    }



}