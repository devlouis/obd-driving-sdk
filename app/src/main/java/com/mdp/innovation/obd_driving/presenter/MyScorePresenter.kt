package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.ui.MyScoreView
import com.mdp.innovation.obd_driving.interactor.MyScoreInteractor

class MyScorePresenter(var myScoreView: MyScoreView?, val myScoreInteractor: MyScoreInteractor) : MyScoreInteractor.OnMyScoreFinishedListener {
    fun isConnected() {
        myScoreView?.showProgress()
        myScoreInteractor.isConnected( this)
    }
    fun onDestroy() {
        myScoreView = null
    }
    override fun onDeviceConnected() {
        myScoreView?.hideProgress()
        myScoreView?.onDeviceConnected()
    }
    override fun onDeviceNoConnected() {
        myScoreView?.hideProgress()
        myScoreView?.onDeviceNoConnected()
    }
}