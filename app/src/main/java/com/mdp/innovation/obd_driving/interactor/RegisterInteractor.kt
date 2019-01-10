package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving.service.WSService
import com.mdp.innovation.obd_driving.service.model.LoginResponse
import com.mdp.innovation.obd_driving.service.model.MyTripsResponse
import com.mdp.innovation.obd_driving.service.model.RegisterResponse
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RegisterInteractor {

    private val TAG = javaClass.simpleName

    interface OnRegisterFinishedListener {
        fun onGetRegisterSuccess(response: RegisterResponse)
        fun onGetRegisterError(message: String)
    }

    fun getRegister(listener: OnRegisterFinishedListener, name: String, lastName: String, email: String, password: String) {

        doAsync{

            val service = WSService()
            val call = service.getRegister(name, lastName, email, password)
            uiThread{
                call.enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){

                            if(response.body()!!.success){
                                listener.onGetRegisterSuccess(response.body()!!)
                            }else{
                                listener.onGetRegisterError(response.body()!!.message)
                            }

                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onGetRegisterError(message)
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onGetRegisterError(t.message!!)
                    }
                })
            }

        }
    }
}