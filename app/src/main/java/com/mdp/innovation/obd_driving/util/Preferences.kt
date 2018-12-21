package com.mdp.innovation.obd_driving.util

import android.content.Context

class Preferences {

    val PREFS_FILENAME = "com.mdp.innovation.obd_driving"
    val SCORE_PENDING = "score_pending"
    val LAST_TRIP_CALCULED = "last_trip_calculed"
    val MY_SCORE = "my_score"
    val MY_VIN = "my_vin"

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

}