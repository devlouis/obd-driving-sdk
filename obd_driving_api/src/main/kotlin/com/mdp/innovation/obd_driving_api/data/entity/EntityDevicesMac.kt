package com.mdp.innovation.obd_driving_api.data.entity


import android.os.ParcelUuid
import java.io.Serializable

class EntityDevicesMac: Serializable {
    var name: String? = ""
    var address: String? = ""
    var type: Int? = 0
    var bondState: Int? = 0


    constructor(name: String?, address: String?, type: Int?, bondState: Int?) {
        this.name = name
        this.address = address
        this.type = type
        this.bondState = bondState

    }


    override fun toString(): String {
        return "EntityDevicesMac(name=$name, address=$address, type=$type, bondState=$bondState)"
    }


}