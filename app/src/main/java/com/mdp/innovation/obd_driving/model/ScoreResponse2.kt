package com.mdp.innovation.obd_driving.model

data class ScoreResponse2(val VIN: String, val tripID: String, val eventType: Int, val score: Double, val duración: Int) {
}