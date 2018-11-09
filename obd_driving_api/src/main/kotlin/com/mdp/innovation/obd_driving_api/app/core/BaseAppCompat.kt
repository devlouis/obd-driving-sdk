package com.mdp.innovation.obd_driving_api.app.core

import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import com.mdp.innovation.obd_driving_api.R


open class BaseAppCompat: AppCompatActivity() {
    protected fun nextActivity(activity: Class<*>) {
        this.nextActivity(activity, false)
    }

    protected fun nextData(activity: Class<*>, bundle: Bundle) {
        this.nextData(activity, bundle, false)
    }

    protected fun nextActivity(activity: Class<*>, notDestroy: Boolean) {
        this.startActivity(Intent(this, activity))
        if (!notDestroy) {
            this.finish()
        }

    }

    protected fun resultActivity(activity: Class<*>, code: Int) {
        this.startActivityForResult(Intent(this, activity), code)
    }

    protected fun resultActivityData(activity: Class<*>, code: Int, bundle: Bundle) {
        val intent = Intent(this, activity)
        intent.putExtras(bundle)
        this.startActivityForResult(intent, code)
    }

    protected fun nextData(activity: Class<*>, bundle: Bundle, notDestroy: Boolean) {
        val intent = Intent(this, activity)
        intent.putExtras(bundle)
        this.startActivity(intent)
        if (!notDestroy) {
            this.finish()
        }

    }

    fun snackBarFail(msj: String, rlaContent: View) {
        val snackbar = Snackbar
            .make(rlaContent, msj, Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.color_error))
        snackbar.show()

    }

    fun snackBarSucceso(msj: String, rlaContent: View) {
        val snackbar = Snackbar
            .make(rlaContent, msj, Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.new_verder))
        snackbar.show()

    }
}