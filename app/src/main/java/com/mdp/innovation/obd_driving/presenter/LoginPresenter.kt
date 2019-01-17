package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.LoginInteractor
import com.mdp.innovation.obd_driving.interactor.MyTripsInteractor
import com.mdp.innovation.obd_driving.service.model.LoginResponse
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.ui.LoginView
import com.mdp.innovation.obd_driving.ui.MyTripsView

class LoginPresenter(var myTripsView: LoginView?, val loginInteractor: LoginInteractor) : LoginInteractor.OnLoginFinishedListener {
    fun onDestroy() {
        myTripsView = null
    }
    fun getLogin(username: String, password: String, token: String) {
        myTripsView?.showLoading()
        loginInteractor.getLogin( this, username, password, token)
    }

    override fun onGetLoginSuccess(response: LoginResponse) {
        myTripsView?.hideLoading()
        myTripsView?.onGetLoginSuccess(response)
    }

    override fun onGetLoginError(message: String) {
        myTripsView?.hideLoading()
        myTripsView?.onGetLoginError(message)
    }
}