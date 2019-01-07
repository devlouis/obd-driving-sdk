package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.TripDetailInteractor
import com.mdp.innovation.obd_driving.service.model.TripDetailResponse
import com.mdp.innovation.obd_driving.ui.TripDetailView

class TripDetailPresenter(var tripDetailView: TripDetailView?, val tripDetailInteractor: TripDetailInteractor) : TripDetailInteractor.OnTripFinishedListener {
    fun onDestroy() {
        tripDetailView = null
    }
    fun getTripDetail(tripId: String) {
        tripDetailView?.showLoading()
        tripDetailInteractor.getTripDetail( this, tripId)
    }

    override fun onGetTripDetailSuccess(response: TripDetailResponse) {
        tripDetailView?.hideLoading()
        tripDetailView?.onGetTripDetailSuccess(response)
    }

    override fun onGetTripDetailError(message: String) {
        tripDetailView?.hideLoading()
        tripDetailView?.onGetTripDetailError(message)
    }
}