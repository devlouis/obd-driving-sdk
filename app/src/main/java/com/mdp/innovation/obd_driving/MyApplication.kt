package com.mdp.innovation.obd_driving

import android.app.Application
import com.mdp.innovation.obd_driving.di.generalModule
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import org.koin.standalone.StandAloneContext.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ConnectOBD.initialize(this)

        startKoin(listOf(generalModule))
    }

}