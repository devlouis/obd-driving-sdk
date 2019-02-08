package com.mdp.innovation.obd_driving.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.annotation.Nullable
import android.text.method.PasswordTransformationMethod
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.mdp.innovation.obd_driving.interactor.LoginInteractor
import com.mdp.innovation.obd_driving.model.DataUserModel
import com.mdp.innovation.obd_driving.presenter.LoginPresenter
import com.mdp.innovation.obd_driving.service.model.LoginResponse
import com.mdp.innovation.obd_driving.ui.LoginView
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.Message
import com.mdp.innovation.obd_driving.util.Preferences
import kotlinx.android.synthetic.main.fragment_login.*
import org.koin.android.ext.android.inject
import java.lang.Exception
import com.google.android.gms.tasks.Task
import com.mdp.innovation.obd_driving.util.Connection
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD


class LoginFragment : BaseFragment(), LoginView {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): LoginFragment{
            return LoginFragment()
        }
    }

    private val navigator by inject<Navigator>()
    private val preferences by inject<Preferences>()
    private val presenter = LoginPresenter(this, LoginInteractor())

    lateinit var firebaseToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(object : OnCompleteListener<InstanceIdResult> {
            override fun onComplete(task: Task<InstanceIdResult>) {
                if (!task.isSuccessful) {

                    var message = if(task.exception != null){
                        "getInstanceId failed: " + task.exception!!.message
                    }else{
                        "getInstanceId failed: No error to show"
                    }
                    Log.w(TAG, message, task.exception)
                    Message.toastLong("Hay problemas con su Google Play Service. No recibirá las notificaciones.", context)
                    return
                }

                // Get new Instance ID token
                val token = task.result!!.token

                // Log and toast
                Log.d(TAG, token)
                //Message.toastLong(token, context)
                //firebaseToken = token
            }
        })



        /*doAsync {
            FirebaseInstanceId.getInstance().deleteInstanceId()
        }*/
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_login, container, false)

        return view
    }



    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI(){

        if(preferences.getDataUser(context) != null){
            navigator.navigateToHome(activity)
            activity!!.finish()
            return
        }

        btn_login.setOnClickListener { v ->
            Log.d(TAG, "Clickkkkkkkk")

            if(validate()){
                showLoading()
                validateInternet()
            }

        }

        tv_register.setOnClickListener { v ->
            Log.d(TAG, "To register.")
            navigator.navigateToRegisterUser(fragmentManager!!, R.id.content)
        }

        et_password.transformationMethod = PasswordTransformationMethod()

    }

    private fun validate() : Boolean{
        var result = true

        val username = et_username.text.toString().trim()
        val password = et_password.text.toString().trim()

        if(username.length == 0){
            et_username_layout.isErrorEnabled = true
            et_username_layout.error = resources.getString(R.string.message_fields_filled)
            et_username.requestFocus()
            result = false
        }else{
            et_username_layout.isErrorEnabled = false
            if(password.length == 0){
                et_password_layout.isErrorEnabled = true
                et_password_layout.error = resources.getString(R.string.message_fields_filled)
                et_password.requestFocus()
                result = false
            }else{
                et_password_layout.isErrorEnabled = false
            }
        }

        return result

    }

    override fun onGetLoginSuccess(response: LoginResponse) {
        Log.d(TAG, "Successssssssssssssss")
        //Message.toastLong(response.message, context)

        val dataUser = DataUserModel()
        dataUser.userId = response.data.userId
        dataUser.name = response.data.name
        dataUser.lastName = response.data.lastName
        dataUser.score = response.data.score
        dataUser.vin = response.data.vin
        dataUser.token = firebaseToken
        dataUser.connectionString = response.data.connectionString

        var myScore = "-"
        try{
            myScore = dataUser.score.toString()
        }catch (ex: Exception){}

        ConnectOBD.saveConnectionString(dataUser.connectionString!!)

        preferences.setDataUser(context, dataUser)
        preferences.setMyScore(context, myScore)



        navigator.navigateToHome(activity)
        activity!!.finish()

    }

    override fun onGetLoginError(message: String) {
        Log.d(TAG, "Errorrrrrrrrrrrrr")
        Message.toastLong(message, context)
    }

    override fun showLoading() {
        loading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        loading.visibility = View.GONE
    }

    private fun validateInternet(){
        Connection.validate(activity!!, object : Connection.OnConnectionFinishedListener{
            override fun onConnectionFinished(code: Int) {
                when(code){
                    Connection.OK ->{
                        val username = et_username.text.toString().trim()
                        val password = et_password.text.toString().trim()

                        var tokenPush = preferences.getTokenPush(context)
                        if(tokenPush == "-"){
                            tokenPush = FirebaseInstanceId.getInstance().token!!
                            preferences.setTokenPush(context, tokenPush)
                        }
                        firebaseToken = tokenPush
                        presenter.getLogin(username, password, firebaseToken)
                    }
                    Connection.NO_NETWORK ->{
                        hideLoading()
                        Message.toastLong(resources.getString(R.string.no_network), context)
                    }
                    Connection.NO_CONNECTION ->{
                        hideLoading()
                        Message.toastLong(resources.getString(R.string.no_connection), context)
                    }
                    Connection.EXCEPTION ->{
                        hideLoading()
                        Message.toastLong(resources.getString(R.string.connection_exception), context)
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        presenter.onDestroy()
        super.onDestroyView()
    }


}
