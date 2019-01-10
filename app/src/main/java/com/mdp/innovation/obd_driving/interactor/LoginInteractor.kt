package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving.service.WSService
import com.mdp.innovation.obd_driving.service.model.LoginResponse
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginInteractor {

    private val TAG = javaClass.simpleName

    interface OnLoginFinishedListener {
        fun onGetLoginSuccess(response: LoginResponse)
        fun onGetLoginError(message: String)
    }

    fun getLogin(listener: OnLoginFinishedListener, username: String, password: String) {

        doAsync{

            val service = WSService()
            val call = service.getLogin(username, password)
            uiThread{
                call.enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){

                            if(response.body()!!.success){
                                listener.onGetLoginSuccess(response.body()!!)
                            }else{
                                listener.onGetLoginError(response.body()!!.message)
                            }

                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onGetLoginError(message)
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onGetLoginError(t.message!!)
                    }
                })
            }

        }
    }
}