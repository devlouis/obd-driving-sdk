package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.MyTripsInteractor
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.TripDetailResponse
import com.mdp.innovation.obd_driving.ui.MyTripsView

class MyTripsPresenter(var myTripsView: MyTripsView?, val myTripsInteractor: MyTripsInteractor) : MyTripsInteractor.OnTripFinishedListener {
    fun onDestroy() {
        myTripsView = null
    }
    fun setView(myTripsView: MyTripsView){
        this.myTripsView = myTripsView
    }
    fun getMyTrips(userId: String, page: Int, elements: Int, showLoading: Boolean) {
        if(showLoading) myTripsView!!.showLoading()
        myTripsInteractor.getMyTrips( this, userId, page, elements)
    }

    override fun onGetMyTripsSuccess(response: MyTripsResponse) {
        if(myTripsView == null) return
        myTripsView!!.hideLoading()
        myTripsView!!.onGetMyTripsSuccess(response)
    }

    override fun onGetMyTripsError(message: String) {
        if(myTripsView == null) return
        myTripsView!!.hideLoading()
        myTripsView!!.onGetMyTripsError(message)
    }

    fun getTripDetail(tripId: String) {
        myTripsView?.showLoading()
        myTripsInteractor.getTripDetail( this, tripId)
    }

    override fun onGetTripDetailSuccess(response: TripDetailResponse) {
        if(myTripsView == null) return
        myTripsView?.hideLoading()
        myTripsView?.onGetTripDetailSuccess(response)
    }

    override fun onGetTripDetailError(message: String) {
        if(myTripsView == null) return
        myTripsView?.hideLoading()
        myTripsView?.onGetTripDetailError(message)
    }
}