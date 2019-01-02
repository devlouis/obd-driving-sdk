package com.mdp.innovation.obd_driving.service.retrofit


import com.mdp.innovation.obd_driving.service.model.MyTripsRequest
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.ScoreRequest
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiRetrofit {

    @POST("api")
    open fun getScore(@Body request: ScoreRequest): Call<ScoreResponse>
    @POST("mytrips")
    open fun getMyTrips(@Body request: MyTripsRequest): Call<MyTripsResponse>

}