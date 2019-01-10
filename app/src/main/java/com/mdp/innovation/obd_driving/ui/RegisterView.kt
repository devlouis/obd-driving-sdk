package com.mdp.innovation.obd_driving.ui

import com.mdp.innovation.obd_driving.service.model.RegisterResponse

interface RegisterView : LoadingView{
    fun onGetRegisterSuccess(response: RegisterResponse)
    fun onGetRegisterError(message: String)
}