package com.mdp.innovation.obd_driving.ui.activity

import android.content.Intent
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.control.ModuleVoltageCommand
import android.support.v4.app.FragmentManager
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.internal.CollectTripDataService
import com.mdp.innovation.obd_driving.ui.HomeView
import com.mdp.innovation.obd_driving.ui.fragment.CancelCollectDialogFragment
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.Global

class CollectTripDataActivity : BaseServiceActivity(), HomeView {

    var serviceClass = CollectTripDataService::class.java
    lateinit var myIntent : Intent

    private val navigator = Navigator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collect_trip_data)

        myIntent = Intent(applicationContext, serviceClass)

        val cmds = ArrayList<ObdCommand>()
        cmds.add(ModuleVoltageCommand())

        navigator.navigateToCollectData(supportFragmentManager, R.id.content)

    }

    override fun showProgress() {
        //progress.visibility = View.VISIBLE
    }
    override fun hideProgress() {
        //progress.visibility = View.GONE
    }

    override fun onBackPressed() {
        System.out.println(Global.cancelValidated)
        if(Global.cancelValidated) super.onBackPressed()
        else validateCancel(supportFragmentManager)
    }

    fun stopService(){
        super.stopService(serviceClass, myIntent)
    }



    companion object {
        fun validateCancel(fragmentManager : FragmentManager){
            var dialog = CancelCollectDialogFragment()
            Global.cancelValidated
            dialog.show(fragmentManager,"cancel_collect")
        }
    }

}
