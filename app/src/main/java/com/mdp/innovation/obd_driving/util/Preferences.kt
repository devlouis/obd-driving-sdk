package com.mdp.innovation.obd_driving.util

import android.content.Context

class Preferences {

    val PREFS_FILENAME = "com.mdp.innovation.obd_driving"
    val SCORE_PENDING = "score_pending"
    val SCORE_DATE = "score_date"
    val MY_SCORE = "my_score"

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

    fun setLastScoreDate(context: Context?, scoreDate: String){
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        val editor = prefs!!.edit()
        editor.putString(SCORE_DATE, scoreDate)
        editor.apply()
    }

    fun getLastScoreDate(context: Context?) : String? {
        var prefs = context!!.getSharedPreferences(PREFS_FILENAME, 0)
        return prefs.getString(SCORE_DATE, "-")
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

}