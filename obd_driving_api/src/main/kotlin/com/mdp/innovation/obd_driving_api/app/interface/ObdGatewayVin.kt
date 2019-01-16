package com.mdp.innovation.obd_driving_api.app.`interface`

interface ObdGatewayVin {
    fun getVin(vin: String)
    fun errorConnect(message: String, type: Int)
}