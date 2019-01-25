package com.mdp.innovation.obd_driving.util

import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast

class Message {

    companion object {
        fun toastShort(message:String, context: Context?){
            val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            val textView = toast.view.findViewById<TextView>(android.R.id.message)
            if( textView != null) textView.gravity = Gravity.CENTER
            toast.show()
        }
        fun toastLong(message:String, context: Context?){
            val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
            val textView = toast.view.findViewById<TextView>(android.R.id.message)
            if( textView != null) textView.gravity = Gravity.CENTER
            toast.show()
        }
    }


}