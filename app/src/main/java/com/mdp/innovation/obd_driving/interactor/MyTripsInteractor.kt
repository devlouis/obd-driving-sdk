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


class MyTripsInteractor {

    private val TAG = javaClass.simpleName

    interface OnTripFinishedListener {
        fun onGetMyTripsSuccess(response: MyTripsResponse)
        fun onGetMyTripsError(message: String)
    }

    fun getMyTrips(listener: OnTripFinishedListener, VIN: String, page: Int, elements: Int) {

        doAsync{

            val service = WSService()
            val call = service.getMyTrips(VIN, page, elements)
            uiThread{
                call.enqueue(object : Callback<MyTripsResponse> {
                    override fun onResponse(call: Call<MyTripsResponse>, response: Response<MyTripsResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){
                            listener.onGetMyTripsSuccess(response.body()!!)
                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onGetMyTripsError(message)
                        }
                    }

                    override fun onFailure(call: Call<MyTripsResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onGetMyTripsError(t.message!!)
                    }
                })
            }

        }
    }
}