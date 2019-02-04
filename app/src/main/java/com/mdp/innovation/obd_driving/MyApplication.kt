package com.mdp.innovation.obd_driving

import android.app.Application
import com.mdp.innovation.obd_driving.di.generalModule
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.data.store.TripRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.LocationRepository
import com.mdp.innovation.obd_driving_api.data.store.repository.ObdRepository
import com.squareup.leakcanary.LeakCanary
import org.koin.standalone.StandAloneContext.startKoin



class MyApplication : Application() {

    var tripRepository: TripRepository? = null
        private set

    var obdRepository: ObdRepository? = null
        private set

    var locationRepository: LocationRepository? = null
        private set

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        startKoin(listOf(generalModule))

        /**
         * Init SDK
         */
        ConnectOBD.initialize(this)
        tripRepository =  TripRepository(this)
        obdRepository =  ObdRepository(this)
        locationRepository =  LocationRepository(this)
    }



}