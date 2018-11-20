package com.mdp.innovation.obd_driving

import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.control.ModuleVoltageCommand
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseAppCompat(), ObdGatewayVin {


    val TAG = javaClass.simpleName
    var coneec = ConnectOBD

    override fun getVin(vin: String) {
        Log.v(TAG, " VIN___: $vin")
        runOnUiThread {
            tviVIN.text = vin
            ConnectOBD.doUnbindService()
        }
    }

    override fun errorConnect(message: String) {
        Log.v(TAG, " errorConnect: $message")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onClickListener()

    }


    fun onClickListener(){

        button.setOnClickListener {
            ConnectOBD.startLiveData(this)
            //Log.v(TAG, " verifyMacOBD: ${ConnectOBD.verifyMacOBD()}")
        }

        btnGoPair.setOnClickListener {
            nextActivity(PairObdActivity::class.java, true)
        }
    }



}
