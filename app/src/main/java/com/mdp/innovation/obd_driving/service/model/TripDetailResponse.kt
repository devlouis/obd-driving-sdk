package com.mdp.innovation.obd_driving.service.model

import java.util.*

data class TripDetailResponse(val latStart: Float, val lonStart: Float, val latEnd: Float, val lonEnd: Float,
                              val polygon: String, val scores: Score, val events: Event) {

    data class Score(val acceleration: Float, val braking: Float, val takingCurves: Float, val speeding: Float)
    data class Event(val acceleration: List<EventItem>, val braking: List<EventItem>,
                     val takingCurves: List<EventItem>,val speeding: List<EventItem>)
    data class EventItem(val lat: Float,val lon: Float,val start: String,val duration: String,val speed: String)

}