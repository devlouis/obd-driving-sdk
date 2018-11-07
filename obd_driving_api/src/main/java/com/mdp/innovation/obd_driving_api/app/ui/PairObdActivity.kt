package com.mdp.innovation.obd_driving_api.app.ui

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import java.util.ArrayList
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mdp.innovation.obd_driving_api.app.ui.adapter.PairedAdapter
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.data.entity.EntityDevicesMac
import kotlinx.android.synthetic.main.activity_pair_obd.*


class PairObdActivity : BaseAppCompat() {

    val BLUETOOTH_LIST_KEY = "bluetooth_list_preference"

    var pairedAdapter: PairedAdapter? = null
    val NewsDeviceStrings = ArrayList<EntityDevicesMac>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_obd)
        initUI()
        onClickListener()

        //F8:CF:C5:59:54:EB
        val mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        mBtAdapter.startDiscovery()





    }

    fun onClickListener(){
        llaBluetoothDiscovery.setOnClickListener {
            viewDialogDevicesNew.visibility = View.VISIBLE
        }
        viewDialogDevicesNew.setOnClickListener {
            viewDialogDevicesNew.visibility = View.GONE
        }
    }

    fun initUI(){
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        rviDevicesNew.setHasFixedSize(true)
        rviDevicesNew.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        pairedAdapter = PairedAdapter(this, NewsDeviceStrings){

        }
        rviDevicesNew.adapter = pairedAdapter
    }


    /**
     * Listar Bluetooth disponibles
     */

    // Create a BroadcastReceiver for ACTION_FOUND
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Get the BluetoothDevice object from the Intent
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // Add the name and address to an array adapter to show in a ListView

                var deviceMac = EntityDevicesMac(device.name, device.address, device.type, device.bondState)
                NewsDeviceStrings.add(deviceMac)
                rviDevicesNew.adapter = pairedAdapter
                //pairedAdapter!!.notifyDataSetChanged()

                LogUtils().v( "ConfigActivity" , " :: ${NewsDeviceStrings.toString()}")

            }
        }
    }


}
