package com.mdp.innovation.obd_driving_api.data.entity

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


/**
 * Created by louislopez on 13,December,2018
 * MDP Consulting,
 * Peru, Lima.
 */
open class TripDrivingEntity: RealmObject() {

    var id: Long = 0
    var id_trip: String = ""
    var live_date: String = ""
    var latitudd: String = ""
    var longitud: String = ""
    var rpm: String = ""
    var kmh: String = ""
    var status: String = ""
    override fun toString(): String {
        return "TripDrivingEntity(id=$id, id_trip='$id_trip', live_date='$live_date', latitudd='$latitudd', longitud='$longitud', rpm='$rpm', kmh='$kmh', status='$status')"
    }


}
