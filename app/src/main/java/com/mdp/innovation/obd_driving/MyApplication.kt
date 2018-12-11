package com.mdp.innovation.obd_driving

import android.app.Application
import com.mdp.innovation.obd_driving.di.generalModule
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import io.realm.Realm
import org.koin.standalone.StandAloneContext.startKoin
import io.realm.Realm.setDefaultConfiguration
import io.realm.RealmConfiguration



class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ConnectOBD.initialize(this)
        startKoin(listOf(generalModule))

        /*Realm.init(this)
        val config = RealmConfiguration.Builder().name("obd_driving.realm").build()
        setDefaultConfiguration(config)*/
    }


}