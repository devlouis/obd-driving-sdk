package com.mdp.innovation.obd_driving.ui

import com.mdp.innovation.obd_driving.service.model.MyScoreResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.model.UpdateVinResponse

interface MyScoreView : LoadingView{
    fun onDeviceConnected()
    fun onDeviceNoConnected()
    fun onGetMyScoreSuccess(response: MyScoreResponse)
    fun onGetMyScoreError(message: String)
}