package com.mdp.innovation.obd_driving_api.app.core

import android.support.v7.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle



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
}