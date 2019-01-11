package com.mdp.innovation.obd_driving.ui

import com.mdp.innovation.obd_driving.service.model.MyScoreResponse
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.model.UpdateVinResponse

interface CollectDataView : LoadingView{
    fun onUpdateVinSuccess(response: UpdateVinResponse)
    fun onUpdateVinError(message: String)
}