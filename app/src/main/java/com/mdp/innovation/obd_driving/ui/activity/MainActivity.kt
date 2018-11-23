package com.mdp.innovation.obd_driving.ui.activity

import android.support.v7.app.ActionBarDrawerToggle
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.control.ModuleVoltageCommand
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import android.content.Context
import android.widget.Toast
import android.support.v4.view.GravityCompat
import android.util.Log
import android.view.MotionEvent
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.interactor.MainInteractor
import com.mdp.innovation.obd_driving.ui.MainView
import com.mdp.innovation.obd_driving.presenter.MainPresenter
import com.mdp.innovation.obd_driving.util.CustomAnimate
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity

class MainActivity : BaseAppCompat(),MainView {

    private val presenter = MainPresenter(this, MainInteractor())

    var aux = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cmds = ArrayList<ObdCommand>()
        cmds.add(ModuleVoltageCommand())

        setToolbarConfig()

        CustomAnimate.setButtonAnimation(btnConnect)

        initUI()

        btnConnect.setOnClickListener {
            //nextActivity(PairObdActivity::class.java, true)
            it.isEnabled = false
            it.postDelayed({
                nextActivity(PairObdActivity::class.java, true)
                it.isEnabled = true
            }, 100L)
        }
    }

    private fun initUI(){
        var result  =  ConnectOBD.verifyMacOBD()
        Log.i("[INFO]", "tengo Mac Device? "+ result.result)
        Log.i("[INFO]", "Mac Device: "+ result.macBluetooth)

        presenter.validateDevicePaired()
    }

    override fun onResume() {
        super.onResume()
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

    override fun onDevicePaired(){
        nextActivity(HomeActivity::class.java, false)
    }

    override fun onDeviceNoPaired(){
        //nextActivity(PairObdActivity::class.java, true)
        toast("Aún no has vinculado ningún OBD.")
    }

    private fun setToolbarConfig(){
        setSupportActionBar(toolbar);
        val actionBar = supportActionBar
        actionBar!!.title = resources.getString(R.string.dcp)
        actionBar.elevation = 4.0F
        actionBar.setDisplayShowHomeEnabled(true)
    }

}
