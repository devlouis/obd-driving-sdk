package com.mdp.innovation.obd_driving.service.retrofit


import com.mdp.innovation.obd_driving.service.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiRetrofit {

    @POST("api")
    open fun getScore(@Body request: ScoreRequest): Call<ScoreResponse>
    @POST("mytrips")
    open fun getMyTrips(@Body request: MyTripsRequest): Call<MyTripsResponse>
    @POST("tripdetail")
    open fun getTripDetail(@Body request: TripDetailRequest): Call<TripDetailResponse>
    @POST("login")
    open fun getLogin(@Body request: LoginRequest): Call<LoginResponse>
    @POST("registrar")
    open fun getRegister(@Body request: RegisterRequest): Call<RegisterResponse>
    @POST("scoreHistorico")
    open fun getMyScore(@Body request: MyScoreRequest): Call<MyScoreResponse>

}