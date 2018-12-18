package com.mdp.innovation.obd_driving.ui.fragment

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.view.View
import com.mdp.innovation.obd_driving.R

import com.mdp.innovation.obd_driving.internal.CollectTripDataService
import com.mdp.innovation.obd_driving.util.Constants
import com.mdp.innovation.obd_driving.util.Message
import com.mdp.innovation.obd_driving.util.Preferences
import org.koin.android.ext.android.inject

open class BaseFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun drawerConfig(activity: FragmentActivity?, toolbar : Toolbar){
        var drawerLayout = activity?.findViewById(R.id.drawer_layout) as DrawerLayout
        toolbar.setNavigationOnClickListener {
            System.out.println("Menuuuuuuuu")
            drawerLayout.openDrawer(Gravity.START)
        }
    }

}
