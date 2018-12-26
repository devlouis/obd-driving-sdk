package com.mdp.innovation.obd_driving.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import android.view.Gravity
import com.mdp.innovation.obd_driving.R

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
