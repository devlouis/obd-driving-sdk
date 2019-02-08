package com.mdp.innovation.obd_driving.service

import android.util.Log
import com.google.gson.Gson
import com.mdp.innovation.obd_driving.service.model.*
import com.mdp.innovation.obd_driving.service.retrofit.ApiRetrofit
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WSService {
    private val TAG = javaClass.simpleName
    //private val WS_URL_SCORE = "https://1ae4befc.ngrok.io"
    //private val WS_URL_SCORE = "https://dcp-test.azurewebsites.net"
    private val WS_URL_SCORE = "https://dcp-api.azurewebsites.net"
    //private val WS_URL_SCORE = "http://192.168.137.1:8080"

    private val gson = Gson()

    /*fun getScore(request: ScoreRequest): ScoreResponse? {
        val retrofit: Retrofit? = Retrofit.Builder()
            .baseUrl(WS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val intentService: ApiRetrofit? = retrofit?.create(ApiRetrofit::class.java)
        val call = intentService?.getScore(request)

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

    fun getMyTrips(userId: String, page: Int, elements: Int) : Call<MyTripsResponse>{
        var request = MyTripsRequest(userId, page,elements)
        Log.d("WS", gson.toJson(request))
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getMyTrips(request)
    }

    fun getTripDetail(tripId: String) : Call<TripDetailResponse>{
        var request = TripDetailRequest(tripId)
        Log.d("WS", gson.toJson(request))
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getTripDetail(request)
    }

    fun getLogin(username: String, password: String, token: String) : Call<LoginResponse>{
        var request = LoginRequest(username, password, token)
        Log.d("WS", gson.toJson(request))
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getLogin(request)
    }

    fun getRegister(name: String, lastName: String, email: String, password: String) : Call<RegisterResponse>{
        var request = RegisterRequest(name, lastName, email, password)
        Log.d("WS", gson.toJson(request))
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getRegister(request)
    }

    fun getMyScore(userId: String) : Call<MyScoreResponse>{
        var request = MyScoreRequest(userId)
        Log.d("WS", gson.toJson(request))
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getMyScore(request)
    }

    fun getDeleteToken(userId: String, token: String) : Call<DeleteTokenResponse>{
        var request = DeleteTokenRequest(userId, token)
        Log.d("WS", gson.toJson(request))
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getDeleteToken(request)
    }

    fun getUpdateToken(userId: String, token: String, oldToken: String) : Call<UpdateTokenResponse>{
        var request = UpdateTokenRequest(userId, token, oldToken)
        Log.d("WS", gson.toJson(request))
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.getUpdateToken(request)
    }

    fun updateVin(userId: String, vin: String) : Call<UpdateVinResponse>{
        var request = UpdateVinRequest(userId, vin)
        Log.d("WS", gson.toJson(request))
        val retrofit = getRetrofit(WS_URL_SCORE)

        val service: ApiRetrofit? = retrofit.create(ApiRetrofit::class.java)
        return service!!.updateVin(request)
    }

}