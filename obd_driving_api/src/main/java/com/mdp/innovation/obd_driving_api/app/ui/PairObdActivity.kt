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

class PairObdActivity : PreferenceActivity() {

    val BLUETOOTH_LIST_KEY = "bluetooth_list_preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_pair_obd)
        addPreferencesFromResource(R.xml.preferences)

        val pairedDeviceStrings = ArrayList<CharSequence>()
        val vals = ArrayList<CharSequence>()

        val listBtDevices = preferenceScreen
            .findPreference(BLUETOOTH_LIST_KEY) as ListPreference

        /*
     * Let's use this device Bluetooth adapter to select which paired OBD-II
     * compliant device we'll use.
     */
        val mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBtAdapter == null) {
            listBtDevices.entries = pairedDeviceStrings.toTypedArray()
            listBtDevices.entryValues = vals.toTypedArray()

            // we shouldn't get here, still warn user
            Toast.makeText(
                this, "This device does not support Bluetooth.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        /*
     * Listen for preferences click.
     *
     * TODO there are so many repeated validations :-/
     */
        val thisActivity = this
        listBtDevices.entries = arrayOfNulls(1)
        listBtDevices.entryValues = arrayOfNulls(1)
        listBtDevices.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            Log.v(" ConfigActivity", " Click Devices List")
            // see what I mean in the previous comment?
            if (mBtAdapter == null || !mBtAdapter.isEnabled) {
                Toast.makeText(
                    thisActivity,
                    "This device does not support Bluetooth or it is disabled.",
                    Toast.LENGTH_LONG
                ).show()
                return@OnPreferenceClickListener false
            }
            true
        }

        /**
         * Get paired devices and populate preference list.
         */
        val pairedDevices = mBtAdapter.bondedDevices
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                pairedDeviceStrings.add(device.name + "\n" + device.address)
                vals.add(device.address)
            }
        }
        listBtDevices.entries = pairedDeviceStrings.toTypedArray()
        listBtDevices.entryValues = vals.toTypedArray()
    }


}
