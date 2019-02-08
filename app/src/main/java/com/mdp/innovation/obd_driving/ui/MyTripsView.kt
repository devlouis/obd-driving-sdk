package com.mdp.innovation.obd_driving.ui

import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.TripDetailResponse

interface MyTripsView : LoadingView{
    fun onGetMyTripsSuccess(response: MyTripsResponse)
    fun onGetMyTripsError(message: String)
    fun onGetTripDetailSuccess(response: TripDetailResponse)
    fun onGetTripDetailError(message: String)
}