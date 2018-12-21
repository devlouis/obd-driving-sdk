package com.mdp.innovation.obd_driving_api.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

import java.io.Serializable


/**
 * Created by louislopez on 13,December,2018
 * MDP Consulting,
 * Peru, Lima.
 */

class TripDrivingEntity: Serializable {


    var id: Long = 0

    var id_trip: String = ""
    var live_date: String = ""
    var latitudd: String = ""
    var longitud: String = ""
    var rpm: String = ""
    var kmh: String = ""
    var status: String = ""

    var dataNew: String = ""
    var dataUdate: String = ""

    override fun toString(): String {
        return "TripDrivingEntity(id=$id, id_trip='$id_trip', live_date='$live_date', latitudd='$latitudd', longitud='$longitud', rpm='$rpm', kmh='$kmh', status='$status', dataNew='$dataNew', dataUdate='$dataUdate')"
    }


}
