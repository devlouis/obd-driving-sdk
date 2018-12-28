package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.TripInteractor
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.ui.TripView

class TripPresenter(var tripView: TripView?, val tripInteractor: TripInteractor) : TripInteractor.OnTripFinishedListener {
    fun onDestroy() {
        tripView = null
    }
    fun getMyTrips(vin: String, page: Int, elements: Int, showLoading: Boolean) {
        if(showLoading) tripView?.showLoading()
        tripInteractor.getMyTrips( this, vin, page, elements)
    }

    override fun onGetMyTripsSuccess(response: MyTripsResponse) {
        tripView?.hideLoading()
        tripView?.onGetMyTripsSuccess(response)
    }

    override fun onGetMyTripsError(message: String) {
        tripView?.hideLoading()
        tripView?.onGetMyTripsError(message)
    }
}