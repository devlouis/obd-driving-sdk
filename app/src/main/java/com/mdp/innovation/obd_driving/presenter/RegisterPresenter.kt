package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.RegisterInteractor
import com.mdp.innovation.obd_driving.service.model.RegisterResponse
import com.mdp.innovation.obd_driving.ui.RegisterView

class RegisterPresenter(var registerView: RegisterView?, val registerInteractor: RegisterInteractor) : RegisterInteractor.OnRegisterFinishedListener {
    fun onDestroy() {
        registerView = null
    }
    fun getRegister(name: String, lastName: String, email: String, password: String) {
        registerInteractor.getRegister( this, name, lastName, email, password)
    }

    override fun onGetRegisterSuccess(response: RegisterResponse) {
        registerView?.hideLoading()
        registerView?.onGetRegisterSuccess(response)
    }

    override fun onGetRegisterError(message: String) {
        registerView?.hideLoading()
        registerView?.onGetRegisterError(message)
    }
}