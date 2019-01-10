package com.mdp.innovation.obd_driving.util

import android.content.Context
import com.google.gson.Gson
import com.mdp.innovation.obd_driving.model.DataUserModel

class Preferences {

    val PREFS_FILENAME = "com.mdp.innovation.obd_driving"
    val SCORE_PENDING = "score_pending"
    val LAST_TRIP_CALCULED = "last_trip_calculed"
    val MY_SCORE = "my_score"
    val MY_VIN = "my_vin"
    val DATA_USER = "data_user"

    val gson = Gson()

    fun setScorePending(context: Context?, pending: Boolean){
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs!!.edit()
        editor.putBoolean(SCORE_PENDING, pending)
        editor.apply()
    }
    fun getScorePending(context: Context?) : Boolean{
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        return prefs.getBoolean(SCORE_PENDING, false)
    }

    fun setLastTripCalculed(context: Context?, scoreDate: String){
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs!!.edit()
        editor.putString(LAST_TRIP_CALCULED, scoreDate)
        editor.apply()
    }
    fun getLastTripCalculed(context: Context?) : String? {
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        return prefs.getString(LAST_TRIP_CALCULED, "-")
    }

    fun setMyScore(context: Context?, score: String){
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs!!.edit()
        editor.putString(MY_SCORE, score)
        editor.apply()
    }
    fun getMyScore(context: Context?) : String? {
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        return prefs.getString(MY_SCORE, "-")
    }

    fun setVIN(context: Context?, vin: String){
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs!!.edit()
        editor.putString(MY_VIN, vin)
        editor.apply()
    }
    fun getVIN(context: Context?) : String? {
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        return prefs.getString(MY_VIN, "-")
    }

    fun setDataUser(context: Context?, data: DataUserModel){
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs!!.edit()

        val json = gson.toJson(data)
        editor.putString(DATA_USER, json)
        editor.apply()
    }
    fun getDataUser(context: Context?) : DataUserModel? {
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)

        val json = prefs.getString(DATA_USER, "")
        return if(json == "") null
        else gson.fromJson(json, DataUserModel::class.java)
    }

    fun removeAll(context: Context?){
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        prefs.edit().clear().commit()
    }

}