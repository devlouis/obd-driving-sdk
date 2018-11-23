package com.mdp.innovation.obd_driving.util

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.Toast

class Message {

    companion object {
        fun toastShort(message:String, context: Context?){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
        fun toastLong(message:String, context: Context?){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun Context.toastShort(message:String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }



}