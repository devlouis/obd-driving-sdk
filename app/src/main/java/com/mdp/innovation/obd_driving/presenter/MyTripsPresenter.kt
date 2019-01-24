package com.mdp.innovation.obd_driving.presenter

import com.mdp.innovation.obd_driving.interactor.MyTripsInteractor
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.ui.MyTripsView

class MyTripsPresenter(var myTripsView: MyTripsView?, val myTripsInteractor: MyTripsInteractor) : MyTripsInteractor.OnTripFinishedListener {
    fun onDestroy() {
        myTripsView = null
    }
    fun getMyTrips(userId: String, page: Int, elements: Int, showLoading: Boolean) {
        if(showLoading) myTripsView!!.showLoading()
        myTripsInteractor.getMyTrips( this, userId, page, elements)
    }

    override fun onGetMyTripsSuccess(response: MyTripsResponse) {
        myTripsView!!.hideLoading()
        myTripsView!!.onGetMyTripsSuccess(response)
    }

    override fun onGetMyTripsError(message: String) {
        myTripsView!!.hideLoading()
        myTripsView!!.onGetMyTripsError(message)
    }
}