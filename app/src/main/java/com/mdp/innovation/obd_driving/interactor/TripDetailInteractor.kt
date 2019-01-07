package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving.service.model.ScoreResponse
import com.mdp.innovation.obd_driving.service.WSService
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.TripDetailResponse
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TripDetailInteractor {

    private val TAG = javaClass.simpleName

    interface OnTripFinishedListener {
        fun onGetTripDetailSuccess(response: TripDetailResponse)
        fun onGetTripDetailError(message: String)
    }

    fun getTripDetail(listener: OnTripFinishedListener, tripId: String) {

        doAsync{

            val service = WSService()
            val call = service.getTripDetail(tripId)
            uiThread{
                call.enqueue(object : Callback<TripDetailResponse> {
                    override fun onResponse(call: Call<TripDetailResponse>, response: Response<TripDetailResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){
                            listener.onGetTripDetailSuccess(response.body()!!)
                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onGetTripDetailError(message)
                        }
                    }

                    override fun onFailure(call: Call<TripDetailResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onGetTripDetailError(t.message!!)
                    }
                })
            }

        }
    }
}