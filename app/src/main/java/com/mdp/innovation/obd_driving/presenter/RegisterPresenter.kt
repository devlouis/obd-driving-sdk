package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.RegisterInteractor
import com.mdp.innovation.obd_driving.service.model.RegisterResponse
import com.mdp.innovation.obd_driving.ui.RegisterView

class RegisterPresenter(var registerView: RegisterView?, val registerInteractor: RegisterInteractor) : RegisterInteractor.OnRegisterFinishedListener {
    fun onDestroy() {
        registerView = null
    }
    fun getRegister(name: String, lastName: String, email: String, password: String) {
        registerView?.showLoading()
        registerInteractor.getRegister( this, name, lastName, email, password)
    }

    override fun onGetRegisterSuccess(response: RegisterResponse) {
        if(registerView == null) return
        registerView?.hideLoading()
        registerView?.onGetRegisterSuccess(response)
    }

    override fun onGetRegisterError(message: String) {
        if(registerView == null) return
        registerView?.hideLoading()
        registerView?.onGetRegisterError(message)
    }
}