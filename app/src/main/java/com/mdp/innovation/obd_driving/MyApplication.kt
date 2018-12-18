package com.mdp.innovation.obd_driving

import android.app.Application
import com.mdp.innovation.obd_driving.di.generalModule
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD

import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.standalone.StandAloneContext.startKoin



class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val config = RealmConfiguration.Builder().name("myTrip.realm").build()
        Realm.setDefaultConfiguration(config)

        ConnectOBD.initialize(this)
        startKoin(listOf(generalModule))
    }


}