package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.TripInteractor
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.ui.TripView

class TripPresenter(var tripView: TripView?, val tripInteractor: TripInteractor) : TripInteractor.OnTripFinishedListener {
    fun onDestroy() {
        tripView = null
    }
    fun getMyTrips(vin: String) {
        //myScoreView?.showProgress()
        tripInteractor.getMyTrips( this, vin)
    }

    override fun onGetMyTripsSuccess(response: MyTripsResponse) {
        //myScoreView?.hideProgress()
        tripView?.onGetMyTripsSuccess(response)
    }

    override fun onGetMyTripsError(message: String) {
        //myScoreView?.hideProgress()
        tripView?.onGetMyTripsError(message)
    }
}