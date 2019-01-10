package com.mdp.innovation.obd_driving.service.model

import java.util.*

data class LoginResponse(val success: Boolean, val data: UserData, val message: String) {

    data class UserData(val userId: String, val name: String, val lastName: String,  val score: Float)

}