package com.mdp.innovation.obd_driving.service.model

data class UpdateTokenRequest(val userId: String, val token: String, val oldToken: String)