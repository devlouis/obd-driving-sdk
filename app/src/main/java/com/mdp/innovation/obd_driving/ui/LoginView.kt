package com.mdp.innovation.obd_driving.ui

import com.mdp.innovation.obd_driving.service.model.LoginResponse

interface LoginView : LoadingView{
    fun onGetLoginSuccess(response: LoginResponse)
    fun onGetLoginError(message: String)
}