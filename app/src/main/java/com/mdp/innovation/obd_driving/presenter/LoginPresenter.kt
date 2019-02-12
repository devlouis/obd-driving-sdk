package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.LoginInteractor
import com.mdp.innovation.obd_driving.interactor.MyTripsInteractor
import com.mdp.innovation.obd_driving.service.model.LoginResponse
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.ui.LoginView
import com.mdp.innovation.obd_driving.ui.MyTripsView

class LoginPresenter(var loginView: LoginView?, val loginInteractor: LoginInteractor) : LoginInteractor.OnLoginFinishedListener {
    fun onDestroy() {
        loginView = null
    }
    fun setView(loginView: LoginView){
        this.loginView = loginView
    }
    fun getLogin(username: String, password: String, token: String) {
        loginView?.showLoading()
        loginInteractor.getLogin( this, username, password, token)
    }

    override fun onGetLoginSuccess(response: LoginResponse) {
        if(loginView == null) return
        //loginView?.hideLoading()
        loginView?.onGetLoginSuccess(response)
    }

    override fun onGetLoginError(message: String) {
        if(loginView == null) return
        loginView?.hideLoading()
        loginView?.onGetLoginError(message)
    }
}