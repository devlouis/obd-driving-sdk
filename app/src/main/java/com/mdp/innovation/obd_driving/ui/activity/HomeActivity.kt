package com.mdp.innovation.obd_driving.ui.activity

import android.Manifest
import android.app.Activity
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.control.ModuleVoltageCommand
import kotlinx.android.synthetic.main.activity_home.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import android.support.v4.view.GravityCompat
import android.view.Gravity
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.internal.CollectTripDataService
import com.mdp.innovation.obd_driving.ui.HomeView
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import java.util.*
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.widget.Toolbar
import com.mdp.innovation.obd_driving.ui.fragment.EndTripDialogFragment
import com.mdp.innovation.obd_driving.ui.fragment.MyScoreFragment
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import com.mdp.innovation.obd_driving_api.data.IoTHub.SendDataOBD
import org.koin.android.ext.android.inject


class HomeActivity : BaseServiceActivity(), HomeView {

    var serviceClass = CollectTripDataService::class.java
    lateinit var myIntent : Intent

    lateinit var myScoreFragment : MyScoreFragment

    private val navigator by inject<Navigator>()
    //private val navigator = Navigator()

    //var sabe = SendDataOBD()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        myIntent = Intent(applicationContext, serviceClass)

        //val cmds = ArrayList<ObdCommand>()
        //cmds.add(ModuleVoltageCommand())

        setDrawerConfig()

        myScoreFragment = navigator.navigateToMyScore(supportFragmentManager, R.id.content)

        //ConnectOBD.getVInDummy()
        //sabe.InitClient()
        //Handler().post(mQueueCommands)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!checkPermission()){
                requestPermission()
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /*if (resultCode == Activity.RESULT_OK){
            val result = data?.getStringExtra("result")
            if(result.equals("end_trip")){

                val handler = Handler()
                handler.postDelayed({
                    var dialog = EndTripDialogFragment()
                    dialog.show(supportFragmentManager,"end_trip")
                    myScoreFragment.showUpdateScoreProgressBar()
                }, 500)


            }
        }*/
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

        navigation_view.setNavigationItemSelectedListener{
            when (it.itemId){
                R.id.action_1 -> {
                    myScoreFragment = navigator.navigateToMyScore(supportFragmentManager, R.id.content)
                }
                R.id.action_2 -> {
                    navigator.navigateToConfiguration(supportFragmentManager, R.id.content)
                }
                /*R.id.action_3 -> toast("Opcion 3 clicked")
                R.id.action_4 ->{
                    toast("Opcion 4 clicked")
                }*/
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

    fun drawerConfig(toolbar : Toolbar){
        var drawerLayout = this.findViewById(R.id.drawer_layout) as DrawerLayout
        toolbar.setNavigationOnClickListener {
            System.out.println("Menuuuuuuuu")
            drawerLayout.openDrawer(Gravity.START)
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)
    }

    /*private val mQueueCommands = object : Runnable {
        override fun run() {

            //sabe.sendData2()
            //Handler().postDelayed(this, 500)
        }
    }*/

}
