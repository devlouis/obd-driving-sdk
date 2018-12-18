package com.mdp.innovation.obd_driving.model

data class ScoreResponse(val VIN: Vin, val tripid: TripId) {

    data class Vin(val aceleracion: String, val frenado: String)
    data class TripId(val aceleracion: String, val frenado: String)

}