package com.mdp.innovation.obd_driving.service.model

import java.util.*

data class MyTripsResponse(val trips: List<Trip>, val total: Int) {

    data class Trip(val tripId: String, val timeStart: Date, val timeEnd: Date,
                    val duration: String, val distance: String, val score: Float)

}