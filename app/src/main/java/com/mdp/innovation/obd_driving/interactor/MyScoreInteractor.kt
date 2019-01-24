package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.WSService
import com.mdp.innovation.obd_driving.service.model.MyScoreResponse
import com.mdp.innovation.obd_driving.service.model.UpdateVinResponse
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyScoreInteractor {

    private val TAG = javaClass.simpleName

    interface OnMyScoreFinishedListener {
        fun onDeviceConnected()
        fun onDeviceNoConnected()
        fun onGetScoreSuccess(response: ScoreResponse)
        fun onGetScoreError(message: String)
        fun onGetMyScoreSuccess(response: MyScoreResponse)
        fun onGetMyScoreError(message: String)
        //fun onUpdateVinSuccess(response: UpdateVinResponse)
        //fun onUpdateVinError(message: String)
    }

    fun isConnected(listener: OnMyScoreFinishedListener) {
        var isConnected = true
        if (isConnected)  listener.onDeviceConnected()
        else listener.onDeviceNoConnected()

        /*var result = ConnectOBD.verifyMacOBD()
        Log.i("macBluetooth:", result.macBluetooth)
        Log.i("result:", result.result.toString())
        if(result.result){
            listener.onDeviceConnected()
        }else{
            listener.onDeviceNoConnected()
        }*/
    }

    fun getScore(listener: OnMyScoreFinishedListener, VIN: String, tripId: String) {

        doAsync{

            val service = WSService()
            val call = service.getScore(VIN, tripId)
            uiThread{
                call.enqueue(object : Callback<ScoreResponse> {
                    override fun onResponse(call: Call<ScoreResponse>, response: Response<ScoreResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){
                            listener.onGetScoreSuccess(response.body()!!)
                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onGetScoreError(message)
                        }
                    }

                    override fun onFailure(call: Call<ScoreResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onGetScoreError(t.message!!)
                    }
                })
            }

        }

    }

    fun getMyScore(listener: OnMyScoreFinishedListener, userId: String) {

        doAsync{

            val service = WSService()
            val call = service.getMyScore(userId)
            uiThread{
                call.enqueue(object : Callback<MyScoreResponse> {
                    override fun onResponse(call: Call<MyScoreResponse>, response: Response<MyScoreResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){
                            listener.onGetMyScoreSuccess(response.body()!!)
                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onGetMyScoreError(message)
                        }
                    }

                    override fun onFailure(call: Call<MyScoreResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onGetMyScoreError(t.message!!)
                    }
                })
            }

        }

    }

    /*fun updateVin(listener: OnMyScoreFinishedListener, userId: String, vin: String) {

        doAsync{

            val service = WSService()
            val call = service.updateVin(userId, vin)
            uiThread{
                call.enqueue(object : Callback<UpdateVinResponse> {
                    override fun onResponse(call: Call<UpdateVinResponse>, response: Response<UpdateVinResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){
                            listener.onUpdateVinSuccess(response.body()!!)
                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onUpdateVinError(message)
                        }
                    }

                    override fun onFailure(call: Call<UpdateVinResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onUpdateVinError(t.message!!)
                    }
                })
            }

        }

    }*/
}