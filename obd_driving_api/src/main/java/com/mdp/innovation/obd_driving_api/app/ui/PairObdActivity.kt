package com.mdp.innovation.obd_driving_api.app.ui

import android.bluetooth.BluetoothAdapter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.util.Log
import android.widget.Toast
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import java.util.ArrayList
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils


class PairObdActivity : BaseAppCompat() {

    val BLUETOOTH_LIST_KEY = "bluetooth_list_preference"


    val NewsDeviceStrings = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_obd)
        //F8:CF:C5:59:54:EB
        val mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        mBtAdapter.startDiscovery()


        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

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
                NewsDeviceStrings.add(device.name + "\n" + device.address)
                LogUtils().v( "ConfigActivity" , " :: ${device.name} - ${device.address}")

            }
        }
    }


}
