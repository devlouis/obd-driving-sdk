package com.mdp.innovation.obd_driving.ui.activity

import android.app.Activity
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.control.ModuleVoltageCommand
import kotlinx.android.synthetic.main.activity_home.*
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.support.v4.view.GravityCompat
import android.view.Gravity
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.internal.CollectTripDataService
import com.mdp.innovation.obd_driving.ui.HomeView
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.Constants
import java.util.*
import android.R.attr.data
import android.os.Handler
import android.util.Log
import com.mdp.innovation.obd_driving.ui.fragment.EndTripDialogFragment
import com.mdp.innovation.obd_driving.ui.fragment.MyScoreFragment
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import com.mdp.innovation.obd_driving_api.app.ui.net.ObdService


class HomeActivity : BaseServiceActivity(), HomeView {

    var serviceClass = CollectTripDataService::class.java
    lateinit var myIntent : Intent

    lateinit var myScoreFragment : MyScoreFragment

    private val navigator = Navigator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        myIntent = Intent(applicationContext, serviceClass)

        val cmds = ArrayList<ObdCommand>()
        cmds.add(ModuleVoltageCommand())

        setDrawerConfig()

        myScoreFragment = navigator.navigateToMyScore(supportFragmentManager, R.id.content)

        ConnectOBD.getVInDummy()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK){
            val result = data?.getStringExtra("result")
            if(result.equals("end_trip")){

                val handler = Handler()
                handler.postDelayed({
                    var dialog = EndTripDialogFragment()
                    dialog.show(supportFragmentManager,"end_trip")
                    myScoreFragment.showUpdateScoreProgressBar()
                }, 500)


            }
        }
    }

    fun startLiveData(){
        //ConnectOBD.startLiveData(this)
    }



    private fun Context.toast(message:String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }

    override fun showProgress() {
        //progress.visibility = View.VISIBLE
    }
    override fun hideProgress() {
        //progress.visibility = View.GONE
    }

    private fun setDrawerConfig(){

        /*val toolbar = includeToolbar as Toolbar

        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.title = resources.getString(R.string.title_home)
        actionBar.elevation = 4.0F
        actionBar.setDisplayShowHomeEnabled(true)

        val drawerToggle:ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        ){
            override fun onDrawerClosed(view:View){
                super.onDrawerClosed(view)
            }

            override fun onDrawerOpened(drawerView: View){
                super.onDrawerOpened(drawerView)
            }
        }

        drawerToggle.isDrawerIndicatorEnabled = true
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()*/


        navigation_view.setNavigationItemSelectedListener{
            when (it.itemId){
                R.id.action_1 -> toast("Opcion 1 clicked")
                R.id.action_2 -> toast("Opcion 2 clicked")
                R.id.action_3 -> toast("Opcion 3 clicked")
                R.id.action_4 ->{
                    toast("Opcion 4 clicked")
                }
            }
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }
    }

    fun goToPairObd(){
        nextActivity(PairObdActivity::class.java, true)
    }

    override fun onBackPressed() {
        if(drawer_layout.isDrawerOpen(Gravity.START)) drawer_layout.closeDrawer(GravityCompat.START)
        else super.onBackPressed()
    }

}
