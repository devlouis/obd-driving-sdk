package com.mdp.innovation.obd_driving.ui

interface MyScoreView : LoadingView{
    fun onDeviceConnected()
    fun onDeviceNoConnected()
}