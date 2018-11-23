package com.mdp.innovation.obd_driving.ui

interface MainView {
    fun showProgress()
    fun hideProgress()
    fun onDevicePaired()
    fun onDeviceNoPaired()
}