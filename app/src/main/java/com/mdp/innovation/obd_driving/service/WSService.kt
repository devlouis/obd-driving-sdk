package com.mdp.innovation.obd_driving.service

import android.util.Log
import com.google.android.gms.fido.u2f.api.common.RegisterRequest
import com.mdp.innovation.obd_driving.service.model.*
import com.mdp.innovation.obd_driving.service.retrofit.ApiRetrofit
import com.mdp.innovation.obd_driving.util.ConstantsWS
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WSService {
    private val TAG = javaClass.simpleName
    private val WS_URL_SCORE = "https://fc63fee2.ngrok.io"
    private val WS_URL_LOCAL = "http://192.168.137.1:8080"

    /*fun getScore(request: ScoreRequest): ScoreResponse? {
        val retrofit: Retrofit? = Retrofit.Builder()
            .baseUrl(WS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: ApiRetrofit? = retrofit?.create(ApiRetrofit::class.java)
        val call = service?.getScore(request)

        call?.enqueue(object : Callback<ScoreResponse> {
            override fun onResponse(call: Call<ScoreResponse>, response: Response<ScoreResponse>) {
                Log.i(TAG, response.body().toString())
            }

            override fun onFailure(call: Call<ScoreResponse>, t: Throwable) {
                Log.e(TAG, t.message)
            }
        })

        val result = call?.execute()?.body()
        Log.d(TAG,result.toString())
        return result
    }*/

    private fun getRetrofit(url: String) : Retrofit{
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getScore(VIN: String, tripId: String) : Call<ScoreResponse>{
        var request = ScoreRequest(VIN, tripId)
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getScore(request)
    }

    fun getMyTrips(VIN: String, page: Int, elements: Int) : Call<MyTripsResponse>{
        var request = MyTripsRequest(VIN, page,elements)
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getMyTrips(request)
    }

    fun getTripDetail(tripId: String) : Call<TripDetailResponse>{
        var request = TripDetailRequest(tripId)
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getTripDetail(request)
    }

    fun getLogin(username: String, password: String) : Call<LoginResponse>{
        var request = LoginRequest(username, password)
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getLogin(request)
    }

    fun getRegister(name: String, lastName: String, email: String, password: String) : Call<RegisterResponse>{
        var request = RegisterRequest(name, lastName, email, password)
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getRegister(request)
    }

    fun getMyScore(userId: String) : Call<MyScoreResponse>{
        var request = MyScoreRequest(userId)
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getMyScore(request)
    }

    fun updateVin(userId: String, vin: String) : Call<UpdateVinResponse>{
        var request = UpdateVinRequest(userId, vin)
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.updateVin(request)
    }

}