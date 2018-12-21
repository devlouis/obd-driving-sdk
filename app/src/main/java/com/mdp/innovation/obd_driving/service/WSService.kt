package com.mdp.innovation.obd_driving.service

import android.util.Log
import com.mdp.innovation.obd_driving.model.ScoreRequest
import com.mdp.innovation.obd_driving.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.retrofit.ApiRetrofit
import com.mdp.innovation.obd_driving.util.ConstantsWS
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WSService {
    private val TAG = javaClass.simpleName
    private val WS_URL = "https://5243b350.ngrok.io"

    fun getScore(request: ScoreRequest): ScoreResponse? {
        val retrofit: Retrofit? = Retrofit.Builder()
            .baseUrl(WS_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: ApiRetrofit? = retrofit?.create(ApiRetrofit::class.java)
        val call = service?.getScore(request)

        /*call?.enqueue(object : Callback<ScoreResponse> {
            override fun onResponse(call: Call<ScoreResponse>, response: Response<ScoreResponse>) {
                Log.i(TAG, response.body().toString())
            }

            override fun onFailure(call: Call<ScoreResponse>, t: Throwable) {
                Log.e(TAG, t.message)
            }
        })*/

        val result = call?.execute()?.body()
        Log.d(TAG,result.toString())
        return result
    }

    fun getScore2(VIN: String, tripId: String) : Call<ScoreResponse>{
        var request = ScoreRequest(VIN, tripId)
        val retrofit: Retrofit? = Retrofit.Builder()
            .baseUrl(ConstantsWS.WS_URL_SCORE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service: ApiRetrofit? = retrofit?.create(ApiRetrofit::class.java)
        return service!!.getScore(request)
    }

}