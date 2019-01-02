package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.ui.MyScoreView
import com.mdp.innovation.obd_driving.interactor.MyScoreInteractor
import com.mdp.innovation.obd_driving.service.model.ScoreResponse

class MyScorePresenter(var myScoreView: MyScoreView?, val myScoreInteractor: MyScoreInteractor) : MyScoreInteractor.OnMyScoreFinishedListener {
    fun isConnected() {
        myScoreView?.showLoading()
        myScoreInteractor.isConnected( this)
    }
    fun onDestroy() {
        myScoreView = null
    }
    override fun onDeviceConnected() {
        myScoreView?.hideLoading()
        myScoreView?.onDeviceConnected()
    }
    override fun onDeviceNoConnected() {
        myScoreView?.hideLoading()
        myScoreView?.onDeviceNoConnected()
    }

    fun getScore(vin: String, tripId: String) {
        //myScoreView?.showProgress()
        myScoreInteractor.getScore( this, vin, tripId)
    }

    override fun onGetScoreSuccess(response: ScoreResponse) {
        //myScoreView?.hideProgress()
        myScoreView?.onGetScoreSuccess(response)
    }

    override fun onGetScoreError(message: String) {
        //myScoreView?.hideProgress()
        myScoreView?.onGetScoreError(message)
    }
}