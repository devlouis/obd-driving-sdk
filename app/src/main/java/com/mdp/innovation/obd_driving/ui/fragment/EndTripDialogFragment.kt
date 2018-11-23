package com.mdp.innovation.obd_driving.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.annotation.Nullable
import com.mdp.innovation.obd_driving.ui.activity.BaseServiceActivity
import com.mdp.innovation.obd_driving.util.CustomAnimate
import com.mdp.innovation.obd_driving.util.Global
import kotlinx.android.synthetic.main.fragment_dialog_end_trip.*


class EndTripDialogFragment : BaseDialogFragment() {

    companion object {
        fun newInstance(): EndTripDialogFragment{
            return EndTripDialogFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dialog_end_trip, container, false)
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUI()
    }

    private fun initUI(){

        CustomAnimate.setButtonAnimation(btnOk)

        btnOk.setOnClickListener {
            it.isEnabled = false
            it.postDelayed({
                dismiss()
                it.isEnabled = true
            }, 100L)
        }

    }
}
