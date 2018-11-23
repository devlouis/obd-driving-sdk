package com.mdp.innovation.obd_driving.ui.navigation

import android.support.v4.app.FragmentManager
import android.support.annotation.IdRes
import com.mdp.innovation.obd_driving.ui.fragment.MyScoreFragment
import com.mdp.innovation.obd_driving.ui.fragment.CollectDataFragment
import android.support.v4.app.ActivityCompat
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import android.support.v4.content.ContextCompat.startActivity
import android.app.Activity
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import com.mdp.innovation.obd_driving.ui.activity.CollectTripDataActivity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.mdp.innovation.obd_driving.ui.activity.MainActivity


class Navigator {

    fun navigateToMyScore(fragmentManager: FragmentManager , @IdRes containerId: Int) : MyScoreFragment{
        val ft = fragmentManager.beginTransaction()
        val myScoreFragment = MyScoreFragment.newInstance()
        ft.replace(containerId, myScoreFragment)
        //ft.addToBackStack(null)
        ft.commit()
        return myScoreFragment
    }

    fun navigateToCollectData(fragmentManager: FragmentManager? , @IdRes containerId: Int){
        val ft = fragmentManager?.beginTransaction()
        ft?.replace(containerId, CollectDataFragment.newInstance())
        //ft?.addToBackStack(null)
        ft?.commit()
    }

    fun toPreviewFragment(fragmentManager: FragmentManager?){
        fragmentManager?.popBackStack()
    }

    fun navigateToCollectTripData(activity: Activity?, requestCode : Int) {
        if (activity != null) {
            var intent = Intent(activity, CollectTripDataActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    fun navigateToMain(activity: Activity?) {
        if (activity != null) {
            var intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

}