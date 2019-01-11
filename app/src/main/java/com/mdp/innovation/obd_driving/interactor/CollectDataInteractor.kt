package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.WSService
import com.mdp.innovation.obd_driving.service.model.MyScoreResponse
import com.mdp.innovation.obd_driving.service.model.UpdateVinResponse
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CollectDataInteractor {

    private val TAG = javaClass.simpleName

    interface OnMyScoreFinishedListener {
        fun onUpdateVinSuccess(response: UpdateVinResponse)
        fun onUpdateVinError(message: String)
    }

    fun updateVin(listener: OnMyScoreFinishedListener, userId: String, vin: String) {

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

    }
}