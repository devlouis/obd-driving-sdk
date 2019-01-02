package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.WSService
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
}