package com.mdp.innovation.obd_driving.ui.navigation

import android.support.v4.app.FragmentManager
import android.support.annotation.IdRes
import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import com.mdp.innovation.obd_driving.model.ItemMyTripsModel
import com.mdp.innovation.obd_driving.service.model.TripDetailResponse
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import com.mdp.innovation.obd_driving.ui.activity.InitialActivity
import com.mdp.innovation.obd_driving.ui.activity.MainActivity
import com.mdp.innovation.obd_driving.ui.activity.TripDetailActivity
import com.mdp.innovation.obd_driving.ui.fragment.*


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
        ft.addToBackStack(null)
        ft.commit()
        return myTripsFragment
    }

    fun navigateToTripDetail(fragmentManager: FragmentManager , @IdRes containerId: Int,
                             trip: ItemMyTripsModel, detail: TripDetailResponse) : TripDetailFragment{
        val ft = fragmentManager.beginTransaction()
        val myTripsFragment = TripDetailFragment.newInstance(trip, detail)
        ft.replace(containerId, myTripsFragment)
        ft.addToBackStack(null)
        ft.commit()
        return myTripsFragment
    }
    fun navigateToTripDetail2(activity: Activity?, trip: ItemMyTripsModel, detail: TripDetailResponse){
        if (activity != null) {
            val gson = Gson()
            var intent = Intent(activity, TripDetailActivity::class.java)
            intent.putExtra("TRIP", gson.toJson(trip))
            intent.putExtra("DETAIL", gson.toJson(detail))
            activity.startActivity(intent)
        }
    }

    fun navigateToConfiguration(fragmentManager: FragmentManager , @IdRes containerId: Int) : ConfigurationFragment{
        val ft = fragmentManager.beginTransaction()
        val configurationFragment = ConfigurationFragment.newInstance()
        ft.replace(containerId, configurationFragment)
        ft.addToBackStack(null)
        ft.commit()
        return configurationFragment
    }

    fun navigateToCollectData(fragmentManager: FragmentManager? , @IdRes containerId: Int){
        val ft = fragmentManager?.beginTransaction()
        ft?.replace(containerId, CollectDataFragment.newInstance(), "collect_data")
        ft?.addToBackStack("collect_data")
        ft?.commit()
    }

    fun toPreviewFragment(fragmentManager: FragmentManager?){
        fragmentManager?.popBackStack()
    }

    fun navigateToMain(activity: Activity?) {
        if (activity != null) {
            var intent = Intent(activity, MainActivity::class.java)
            //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }
    fun navigateToHome(activity: Activity?) {
        if (activity != null) {
            var intent = Intent(activity, HomeActivity::class.java)
            //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }
    fun navigateToInitial(activity: Activity?) {
        if (activity != null) {
            var intent = Intent(activity, InitialActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            activity.startActivity(intent)
        }
    }

    fun navigateToLogin(fragmentManager: FragmentManager , @IdRes containerId: Int) : LoginFragment{
        val ft = fragmentManager.beginTransaction()
        val loginFragment = LoginFragment.newInstance()
        ft.replace(containerId, loginFragment)
        ft.commit()
        return loginFragment
    }

    fun navigateToRegisterUser(fragmentManager: FragmentManager , @IdRes containerId: Int) : RegisterFragment{
        val ft = fragmentManager.beginTransaction()
        val registerFragment = RegisterFragment.newInstance()
        ft.replace(containerId, registerFragment)
        ft.addToBackStack(null)
        ft.commit()
        return registerFragment
    }

}