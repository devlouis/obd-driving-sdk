package com.mdp.innovation.obd_driving_api.data.entity

class FailuresEntity {

    var id_trip: String = ""
    var cantidad: Int = 0
    var aceptados: Int = 0
    var denegados: Int = 0
    var no_enviados: Int = 0
    var sincronnizados: Int = 0
    override fun toString(): String {
        return "FailuresEntity(id_trip=$id_trip, cantidad=$cantidad, aceptados=$aceptados, denegados=$denegados, no_enviados=$no_enviados, sincronnizados=$sincronnizados)"
    }


}