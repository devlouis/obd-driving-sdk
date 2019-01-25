package com.mdp.innovation.obd_driving.interactor

import android.util.Log
import com.mdp.innovation.obd_driving.service.WSService
import com.mdp.innovation.obd_driving.service.model.*
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TokenInteractor {

    private val TAG = javaClass.simpleName

    interface OnDeleteTokenFinishedListener {
        fun onDeleteTokenSuccess(response: DeleteTokenResponse)
        fun onDeleteTokenError(message: String)
    }

    interface OnUpdateTokenFinishedListener {
        fun onUpdateTokenSuccess(response: UpdateTokenResponse)
        fun onUpdateTokenError(message: String)
    }

    fun deleteToken(listener: OnDeleteTokenFinishedListener, userId: String, token: String) {

        doAsync{

            val service = WSService()
            val call = service.getDeleteToken(userId, token)
            uiThread{
                call.enqueue(object : Callback<DeleteTokenResponse> {
                    override fun onResponse(call: Call<DeleteTokenResponse>, response: Response<DeleteTokenResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){
                            if(response.body()!!.success){
                                listener.onDeleteTokenSuccess(response.body()!!)
                            }else{
                                listener.onDeleteTokenError("Ocurrió un error al eliminar el token.")
                            }

                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onDeleteTokenError(message)
                        }
                    }

                    override fun onFailure(call: Call<DeleteTokenResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onDeleteTokenError(t.message!!)
                    }
                })
            }

        }

    }

    fun updateToken(listener: OnUpdateTokenFinishedListener, userId: String, token: String, oldToken: String) {

        doAsync{

            val service = WSService()
            val call = service.getUpdateToken(userId, token, oldToken)
            uiThread{
                call.enqueue(object : Callback<UpdateTokenResponse> {
                    override fun onResponse(call: Call<UpdateTokenResponse>, response: Response<UpdateTokenResponse>) {
                        Log.i(TAG, response.body().toString())
                        if(response.isSuccessful){
                            if(response.body()!!.success){
                                listener.onUpdateTokenSuccess(response.body()!!)
                            }else{
                                listener.onUpdateTokenError("Ocurrió un error al eliminar el token.")
                            }

                        }else{
                            var message = if(response.errorBody() != null){
                                response.message() + " | " + response.errorBody()!!.string()
                            }else{
                                "Error does not supplied."
                            }
                            listener.onUpdateTokenError(message)
                        }
                    }

                    override fun onFailure(call: Call<UpdateTokenResponse>, t: Throwable) {
                        Log.e(TAG, t.message)
                        Log.e(TAG, t.cause.toString())
                        listener.onUpdateTokenError(t.message!!)
                    }
                })
            }

        }

    }
}