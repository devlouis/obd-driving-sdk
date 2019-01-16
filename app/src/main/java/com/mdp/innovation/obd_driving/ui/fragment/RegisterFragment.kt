package com.mdp.innovation.obd_driving.ui.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.annotation.Nullable
import android.text.method.PasswordTransformationMethod
import android.util.Log
import com.mdp.innovation.obd_driving.interactor.LoginInteractor
import com.mdp.innovation.obd_driving.interactor.RegisterInteractor
import com.mdp.innovation.obd_driving.presenter.LoginPresenter
import com.mdp.innovation.obd_driving.presenter.RegisterPresenter
import com.mdp.innovation.obd_driving.service.model.LoginResponse
import com.mdp.innovation.obd_driving.service.model.RegisterResponse
import com.mdp.innovation.obd_driving.ui.LoginView
import com.mdp.innovation.obd_driving.ui.RegisterView
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.Message
import kotlinx.android.synthetic.main.fragment_register.*
import org.koin.android.ext.android.inject

class RegisterFragment : BaseFragment(), RegisterView {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): RegisterFragment{
            return RegisterFragment()
        }
    }

    private val navigator by inject<Navigator>()

    private val presenter = RegisterPresenter(this, RegisterInteractor())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_register, container, false)

        return view
    }



    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI(){

        btn_register.setOnClickListener { v ->
            Log.d(TAG, "CLickkkkkkkk")

            if(validate()){
                val name = et_name.text.toString()
                val lastName = et_lastname.text.toString()
                val email = et_email.text.toString()
                val password = et_password.text.toString()

                presenter.getRegister(name, lastName, email, password)
            }


        }

        et_password.transformationMethod = PasswordTransformationMethod()
        et_rep_password.transformationMethod = PasswordTransformationMethod()

    }

    private fun validate() : Boolean{
        var result = true

        val name = et_name.text.toString().trim()
        val lastName = et_lastname.text.toString().trim()
        val email = et_email.text.toString().trim()
        val password = et_password.text.toString().trim()
        val passwordRepeated = et_rep_password.text.toString().trim()



        if(name.length == 0){
            et_name_layout.isErrorEnabled = true
            et_name_layout.error = resources.getString(R.string.message_fields_filled)
            et_name.requestFocus()
            result = false
        }else{
            et_name_layout.isErrorEnabled = false
            if(lastName.length == 0){
                et_lastname_layout.isErrorEnabled = true
                et_lastname_layout.error = resources.getString(R.string.message_fields_filled)
                et_lastname_layout.requestFocus()
                result = false
            }else{
                et_lastname_layout.isErrorEnabled = false
                if(email.length == 0){
                    et_email_layout.isErrorEnabled = true
                    et_email_layout.error = resources.getString(R.string.message_fields_filled)
                    et_email_layout.requestFocus()
                    result = false
                }else{
                    et_email_layout.isErrorEnabled = false

                    if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                        et_email_layout.isErrorEnabled = true
                        et_email_layout.error = resources.getString(R.string.message_email_valid)
                        et_email_layout.requestFocus()
                        result = false
                    }else{
                        et_email_layout.isErrorEnabled = false
                        if(password.length == 0){
                            et_password_layout.isErrorEnabled = true
                            et_password_layout.error = resources.getString(R.string.message_fields_filled)
                            et_password_layout.requestFocus()
                            result = false
                        }else{
                            et_password_layout.isErrorEnabled = false
                            if(passwordRepeated.length == 0){
                                et_rep_password_layout.isErrorEnabled = true
                                et_rep_password_layout.error = resources.getString(R.string.message_fields_filled)
                                et_rep_password_layout.requestFocus()
                                result = false
                            }else{
                                et_rep_password_layout.isErrorEnabled = false
                                if(password != passwordRepeated){
                                    et_rep_password_layout.isErrorEnabled = true
                                    et_rep_password_layout.error = resources.getString(R.string.message_passwords_repeat)
                                    et_rep_password_layout.requestFocus()
                                    result = false
                                }else{
                                    et_rep_password_layout.isErrorEnabled = false

                                }
                            }
                        }
                    }


                }
            }
        }

        return result

    }

    override fun onGetRegisterSuccess(response: RegisterResponse) {
        Log.d(TAG, "Successssssssssssssss")
        Message.toastLong(response.message, context)
        fragmentManager!!.popBackStack()
    }

    override fun onGetRegisterError(message: String) {
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
