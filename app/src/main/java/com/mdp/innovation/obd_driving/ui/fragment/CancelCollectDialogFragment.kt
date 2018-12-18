package com.mdp.innovation.obd_driving.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.annotation.Nullable
import com.mdp.innovation.obd_driving.ui.InterfaceView
import com.mdp.innovation.obd_driving.ui.activity.BaseServiceActivity
import com.mdp.innovation.obd_driving.ui.activity.CollectTripDataActivity
import com.mdp.innovation.obd_driving.util.CustomAnimate
import com.mdp.innovation.obd_driving.util.Global
import kotlinx.android.synthetic.main.fragment_dialog_cancel_collect.*


class CancelCollectDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(customInterface: InterfaceView): CancelCollectDialogFragment{
            val fragment = CancelCollectDialogFragment()
            fragment.customInterface = customInterface
            return fragment
        }
    }

    private lateinit var customInterface: InterfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_cancel_collect, container, false)
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
                //Global.cancelValidated = true
                customInterface.toDo()
                it.isEnabled = true
            }, 100L)
        }

        btnNo.setOnClickListener {
            it.isEnabled = false
            it.postDelayed({
                dismiss()
                //Global.cancelValidated = false
                it.isEnabled = true
            }, 100L)
        }

    }
}
