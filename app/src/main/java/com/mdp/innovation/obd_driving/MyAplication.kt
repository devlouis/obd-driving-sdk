package com.mdp.innovation.obd_driving

import android.app.Application
import android.content.Context
import android.support.annotation.Nullable
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import org.jetbrains.annotations.Contract



class MyAplication: Application() {
    override fun onCreate() {
        super.onCreate()
        ConnectOBD.initialize(this)
    }
}