package com.mdp.innovation.obd_driving.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.annotation.Nullable
import android.text.method.PasswordTransformationMethod
import android.util.Log
import com.crashlytics.android.Crashlytics
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
import android.widget.Toast
import com.mdp.innovation.obd_driving.ui.activity.MainActivity
import com.google.firebase.internal.FirebaseAppHelper.getToken
import android.support.annotation.NonNull
import com.google.android.gms.tasks.Task
import org.jetbrains.anko.doAsync


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener(object : OnCompleteListener<InstanceIdResult> {
            override fun onComplete(task: Task<InstanceIdResult>) {
                if (!task.isSuccessful) {
                    Log.w(TAG, "getInstanceId failed", task.exception)
                    return
                }

                // Get new Instance ID token
                val token = task.result!!.token

                // Log and toast
                Log.d(TAG, token)
                Message.toastLong(token, context)
            }
        })*/



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
            navigator.navigateToMain(activity)
            activity!!.finish()
            return
        }

        btn_login.setOnClickListener { v ->
            Log.d(TAG, "CLickkkkkkkk")

            if(validate()){
                val username = et_username.text.toString()
                val password = et_password.text.toString()

                presenter.getLogin(username, password)
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
        Message.toastLong(response.message, context)

        val dataUser = DataUserModel()
        dataUser.userId = response.data.userId
        dataUser.name = response.data.name
        dataUser.lastName = response.data.lastName
        dataUser.score = response.data.score
        dataUser.vin = response.data.vin

        var myScore = "-"
        try{
            myScore = dataUser.score.toString()
        }catch (ex: Exception){}

        preferences.setDataUser(context, dataUser)
        preferences.setMyScore(context, myScore)

        navigator.navigateToMain(activity)
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


}
