package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.ui.MainView
import com.mdp.innovation.obd_driving.interactor.MainInteractor

class MainPresenter(var mainView: MainView?, val mainInteractor: MainInteractor) : MainInteractor.OnMainFinishedListener {
    fun validateDevicePaired() {
        mainView?.showProgress()
        mainInteractor.validateDevicePaired( this)
    }
    fun onDestroy() {
        mainView = null
    }
    override fun onDevicePaired() {
        mainView?.onDevicePaired()
    }
    override fun onDeviceNoPaired() {
        mainView?.onDeviceNoPaired()
    }
}