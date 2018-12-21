package com.mdp.innovation.obd_driving.service.retrofit


import com.mdp.innovation.obd_driving.model.ScoreRequest
import com.mdp.innovation.obd_driving.model.ScoreResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiRetrofit {

    @POST("api")
    open fun getScore(@Body request: ScoreRequest): Call<ScoreResponse>

}