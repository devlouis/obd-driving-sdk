package com.mdp.innovation.obd_driving.util

import android.view.MotionEvent
import android.view.View

class CustomAnimate {

    companion object {
        fun setButtonAnimation(view: View){
            view.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.animate().scaleXBy(0.5f).setDuration(100).start()
                        v.animate().scaleYBy(0.5f).setDuration(100).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        v.animate().cancel()
                        v.animate().scaleX(1f).setDuration(100).start()
                        v.animate().scaleY(1f).setDuration(100).start()
                    }
                }
                false
            }
        }
    }



}