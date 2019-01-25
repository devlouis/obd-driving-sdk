package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.ui.MyScoreView
import com.mdp.innovation.obd_driving.interactor.MyScoreInteractor
import com.mdp.innovation.obd_driving.service.model.MyScoreResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.model.UpdateVinResponse

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

    fun getMyScore(userId: String) {
        //myScoreView?.showLoading()
        myScoreInteractor.getMyScore( this, userId)
    }

    override fun onGetMyScoreSuccess(response: MyScoreResponse) {
        //myScoreView?.hideLoading()
        myScoreView?.onGetMyScoreSuccess(response)
    }

    override fun onGetMyScoreError(message: String) {
        //myScoreView?.hideLoading()
        myScoreView?.onGetMyScoreError(message)
    }

}