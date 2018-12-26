package com.mdp.innovation.obd_driving.ui.navigation

import android.support.v4.app.FragmentManager
import android.support.annotation.IdRes
import com.mdp.innovation.obd_driving.ui.fragment.MyScoreFragment
import com.mdp.innovation.obd_driving.ui.fragment.CollectDataFragment
import android.app.Activity
import android.content.Intent
import com.mdp.innovation.obd_driving.ui.activity.MainActivity
import com.mdp.innovation.obd_driving.ui.fragment.ConfigurationFragment
import com.mdp.innovation.obd_driving.ui.fragment.MyTripsFragment


class Navigator {

    fun navigateToMyScore(fragmentManager: FragmentManager , @IdRes containerId: Int) : MyScoreFragment{
        val ft = fragmentManager.beginTransaction()
        val myScoreFragment = MyScoreFragment.newInstance()
        ft.replace(containerId, myScoreFragment)
        //ft.addToBackStack(null)
        ft.commit()
        return myScoreFragment
    }

    fun navigateToMyTrips(fragmentManager: FragmentManager , @IdRes containerId: Int) : MyTripsFragment{
        val ft = fragmentManager.beginTransaction()
        val myTripsFragment = MyTripsFragment.newInstance()
        ft.replace(containerId, myTripsFragment)
        //ft.addToBackStack(null)
        ft.commit()
        return myTripsFragment
    }

    fun navigateToConfiguration(fragmentManager: FragmentManager , @IdRes containerId: Int) : ConfigurationFragment{
        val ft = fragmentManager.beginTransaction()
        val configurationFragment = ConfigurationFragment.newInstance()
        ft.replace(containerId, configurationFragment)
        //ft.addToBackStack(null)
        ft.commit()
        return configurationFragment
    }

    fun navigateToCollectData(fragmentManager: FragmentManager? , @IdRes containerId: Int){
        val ft = fragmentManager?.beginTransaction()
        ft?.replace(containerId, CollectDataFragment.newInstance())
        ft?.addToBackStack("collect_data")
        ft?.commit()
    }

    fun toPreviewFragment(fragmentManager: FragmentManager?){
        fragmentManager?.popBackStack()
    }

    fun navigateToMain(activity: Activity?) {
        if (activity != null) {
            var intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

}