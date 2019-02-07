package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.CollectDataInteractor
import com.mdp.innovation.obd_driving.service.model.UpdateVinResponse
import com.mdp.innovation.obd_driving.ui.CollectDataView

class CollectDataPresenter(var collectDataView: CollectDataView?, val collectDataInteractor: CollectDataInteractor) : CollectDataInteractor.OnMyScoreFinishedListener {
    fun onDestroy() {
        collectDataView = null
    }
    fun updateVin(userId: String, vin: String) {
        //myScoreView?.showLoading()
        collectDataInteractor.updateVin( this, userId, vin)
    }

    override fun onUpdateVinSuccess(response: UpdateVinResponse) {
        if(collectDataView == null) return
        //myScoreView?.hideLoading()
        collectDataView?.onUpdateVinSuccess(response)
    }

    override fun onUpdateVinError(message: String) {
        if(collectDataView == null) return
        //myScoreView?.hideLoading()
        collectDataView?.onUpdateVinError(message)
    }
}