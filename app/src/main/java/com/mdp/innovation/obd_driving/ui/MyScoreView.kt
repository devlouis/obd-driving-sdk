package com.mdp.innovation.obd_driving.ui

import com.mdp.innovation.obd_driving.model.ScoreResponse

interface MyScoreView : LoadingView{
    fun onDeviceConnected()
    fun onDeviceNoConnected()
    fun onGetScoreSuccess(response: ScoreResponse)
    fun onGetScoreError(message: String)
}