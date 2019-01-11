package com.mdp.innovation.obd_driving.service.model

data class MyTripsRequest(val vin: String, val page: Int, val elements: Int)