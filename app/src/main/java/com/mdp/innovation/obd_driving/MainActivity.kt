package com.mdp.innovation.obd_driving

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import com.mdp.innovation.obd_driving_api.app.ui.activity.SensorActivity
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseAppCompat(), ObdGatewayVin {


    val TAG = javaClass.simpleName


    override fun getVin(vin: String) {
        Log.v(TAG, " VIN___: $vin")
        runOnUiThread {
            tviVIN.text = vin
            ConnectOBD.stopLiveData()
        }
    }

    override fun errorConnect(message: String) {
        Log.v(TAG, " errorConnect: $message")
        showDialodAlert("${message}")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        onClickListener()

    }


    fun onClickListener(){

        button.setOnClickListener {
            ConnectOBD.startLiveData(this)

        }

        btnGoPair.setOnClickListener {
            nextActivity(PairObdActivity::class.java, true)
            //nextActivity(SensorActivity::class.java, true)
        }
        btnCheckConecction.setOnClickListener {
            ConnectOBD.CheckConecction()
        }

        btnVerifyMac.setOnClickListener {
            Log.v(TAG, " verifyMacOBD: ${ConnectOBD.verifyMacOBD()}")
            tviVerifyMac.text = "Boolean: ${ConnectOBD.verifyMacOBD().result} \nMac: ${ConnectOBD.verifyMacOBD().macBluetooth}"
        }


    }


    private fun showDialodAlert(msg: String){
        val builderAlertDialog = AlertDialog.Builder(this@MainActivity)
        builderAlertDialog.setTitle("Driving OBD")
        builderAlertDialog.setMessage(msg)
        builderAlertDialog.setPositiveButton("Ok") {dialog, which ->
            dialog.dismiss()
        }
        var dialog = builderAlertDialog.create()
        dialog.show()
    }


}
