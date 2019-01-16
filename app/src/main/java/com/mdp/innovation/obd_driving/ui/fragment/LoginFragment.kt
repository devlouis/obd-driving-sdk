package com.mdp.innovation.obd_driving.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.annotation.Nullable
import android.util.Log
import com.crashlytics.android.Crashlytics
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

            val username = et_email.text.toString()
            val password = et_password.text.toString()

            //presenter.getLogin(username, password)

            //Crashlytics.getInstance().crash()

            val fex : String? = null
            fex!!.length

        }

        tv_register.setOnClickListener { v ->
            Log.d(TAG, "To register.")
            navigator.navigateToRegisterUser(fragmentManager!!, R.id.content)
        }

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
