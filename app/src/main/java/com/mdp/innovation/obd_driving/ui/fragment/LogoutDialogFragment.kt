package com.mdp.innovation.obd_driving.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.annotation.Nullable
import com.google.firebase.iid.FirebaseInstanceId
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.CustomAnimate
import com.mdp.innovation.obd_driving.util.Preferences
import kotlinx.android.synthetic.main.fragment_dialog_cancel_collect.*
import org.jetbrains.anko.doAsync
import org.koin.android.ext.android.inject


class LogoutDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(): LogoutDialogFragment{
            return LogoutDialogFragment()
        }
    }

    private val navigator by inject<Navigator>()
    private val preferences by inject<Preferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_logout, container, false)
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI(){

        CustomAnimate.setButtonAnimation(btnNo)
        CustomAnimate.setButtonAnimation(btnYes)

        btnYes.setOnClickListener {
            it.isEnabled = false
            it.postDelayed({
                dismiss()

                doAsync {
                    FirebaseInstanceId.getInstance().deleteInstanceId()
                }

                preferences.removeAll(context)
                navigator.navigateToInitial(activity)
                activity!!.finish()

                it.isEnabled = true
            }, 100L)
        }

        btnNo.setOnClickListener {
            it.isEnabled = false
            it.postDelayed({
                dismiss()
                it.isEnabled = true
            }, 100L)
        }

    }
}
