package com.mdp.innovation.obd_driving_api.app.core

import android.app.Application
import com.mdp.innovation.obd_driving_api.data.store.TripRepository


/**
 * Created by louislopez on 13,December,2018
 * MDP Consulting,
 * Peru, Lima.
 */
class MyApplicationSDK : Application() {


    lateinit var tripRepository: TripRepository
    override fun onCreate() {
        super.onCreate()

        tripRepository =  TripRepository(this)
    }

}