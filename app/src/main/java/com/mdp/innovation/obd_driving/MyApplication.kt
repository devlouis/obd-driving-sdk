package com.mdp.innovation.obd_driving

import android.app.Application
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ConnectOBD.initialize(this)
    }


}