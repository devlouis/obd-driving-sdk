package com.mdp.innovation.obd_driving.ui

import com.mdp.innovation.obd_driving.service.model.MyTripsResponse

interface TripView : LoadingView{
    fun onGetMyTripsSuccess(response: MyTripsResponse)
    fun onGetMyTripsError(message: String)
}