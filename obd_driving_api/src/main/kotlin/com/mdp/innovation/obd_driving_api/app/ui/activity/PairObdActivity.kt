package com.mdp.innovation.obd_driving_api.app.ui.activity

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import android.bluetooth.BluetoothDevice
import android.content.*
import android.os.Handler
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.`interface`.OnItemClickListenerPair
import com.mdp.innovation.obd_driving_api.app.ui.adapter.PairedAdapter
import com.mdp.innovation.obd_driving_api.app.ui.adapter.SearchBlutoothAdapter
import com.mdp.innovation.obd_driving_api.app.ui.config.ObdConfig
import com.mdp.innovation.obd_driving_api.app.ui.io.*
import com.mdp.innovation.obd_driving_api.app.utils.DevicePair
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.commands.SpeedCommand
import com.mdp.innovation.obd_driving_api.commands.control.VinCommand
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference
import com.mdp.innovation.obd_driving_api.enums.AvailableCommandNames
import kotlinx.android.synthetic.main.activity_pair_obd.*
import java.util.*
import kotlin.concurrent.schedule


class PairObdActivity : BaseAppCompat() {

    val TAG = javaClass.simpleName
    val BLUETOOTH_LIST_KEY = "bluetooth_list_preference"
    var VIN = ""

    var pairedNewAdapter: SearchBlutoothAdapter? = null
    var pairedAdapter: PairedAdapter? = null
    var NewsDeviceStrings = ArrayList<BluetoothDevice>()

    lateinit var mBtAdapter: BluetoothAdapter

    lateinit var appSharedPreference: SharedPreference
    private var bluetoothDefaultIsEnable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_obd)
        LogUtils().v(TAG, " Hola")
        initUI()
        onClickListener()

        //F8:CF:C5:59:54:EB

    }

    fun onClickListener(){
        cviDialog.setOnClickListener {  }
        llaBluetoothDiscovery.setOnClickListener {
            rviDevicesNew.removeAllViews()
            mBtAdapter.startDiscovery()
            //startLiveData()
            Handler().postDelayed({
                viewDialogDevicesNew.visibility = View.VISIBLE
            }, 600)



        }
        viewDialogDevicesNew.setOnClickListener {
            viewDialogDevicesNew.visibility = View.GONE
            mBtAdapter.cancelDiscovery()
        }
        tviBluetoothStatus.setOnClickListener {
            if (!mBtAdapter.isEnabled){
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, 1000)
            }

        }
    }

    fun BluetoohEnable(){
        tviBluetoothStatus.text = "Bluetooth disponible"
        tviBluetoothStatus.setTextColor(resources.getColor(R.color.new_verder))
        tviBluetoothStatus.isClickable = false
    }

    fun BluetoothDisabled(){
        tviBluetoothStatus.text = "Activar Bluetooth"
        tviBluetoothStatus.setTextColor(resources.getColor(R.color.error_color_material))
        tviBluetoothStatus.isClickable = true
    }

    fun initUI(){

        //Validar mac existente.
        appSharedPreference = SharedPreference(this)
        macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        LogUtils().v(TAG, " macDevice:: $macDevice")

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null)
            bluetoothDefaultIsEnable = btAdapter.isEnabled

        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        if (mBtAdapter.isEnabled){
            BluetoohEnable()
        }else{
            BluetoothDisabled()
        }

        BluetoothPaired()

        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)

        rviDevicesNew.setHasFixedSize(true)
        rviDevicesNew.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        pairedNewAdapter = SearchBlutoothAdapter(this, NewsDeviceStrings) { bluetoothDevice: BluetoothDevice ->
            LogUtils().v(TAG, " select: ${bluetoothDevice.name} - ${bluetoothDevice.address}")
            macDevice = bluetoothDevice.address
            LogUtils().v(TAG, " MAC_: ${macDevice}")
            val device = bluetoothDevice
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                DevicePair().unpairDevice(device)
            } else {
                snackBarSucceso("Vinculando...", claContent)
                //pairDevice(device)
                DevicePair().pairDevice(device)
            }
        }
        rviDevicesNew.adapter = pairedNewAdapter
        //registerReceiver(mReceiver, IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))

    }


    /**
     * Listar Bluetooth disponibles
     */

    // Create a BroadcastReceiver for ACTION_FOUND
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                if (state == BluetoothAdapter.STATE_ON) {
                    BluetoohEnable()
                }
            }else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    snackBarSucceso("Vinculado", claContent)
                    appSharedPreference.saveMacBluetooth(device.address)
                    BluetoothPaired()
                    viewDialogDevicesNew.visibility = View.GONE

                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    snackBarFail("Desvinculado", claContent)
                }
                //mAdapter.notifyDataSetChanged()
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                /**
                 * muestra carga mientras busca
                 */
                NewsDeviceStrings.clear()
                vLoadingDevices.visibility = View.VISIBLE
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action){
                /**
                 * termina busqueda
                 */
                vLoadingDevices.visibility = View.GONE
            }
            else if (BluetoothDevice.ACTION_FOUND == action) {
                /**
                 *  - Cuando el descubrimiento encuentra un dispositivo
                 *  - Obtener el objeto dispositivo Bluetooth de la intención
                 */
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //
                //Agregue el nombre y la dirección a un adaptador de matriz para mostrar en un Recyclerview, que no este emparejado
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    LogUtils().v(" BOND_BONDED ", " :: ${device.name}")
                    NewsDeviceStrings.add(device)
                    rviDevicesNew.adapter = pairedNewAdapter
                }
            }
        }
    }

    /**
     * Lista de bluetooth emparejados
     */

    var macDevice = ""
    fun BluetoothPaired(){
        val pairedDevices = mBtAdapter.bondedDevices
        if (pairedDevices == null || pairedDevices.size == 0) {
            snackBarFail("No Paired Devices Found", claContent)
        } else {
            val list = ArrayList<BluetoothDevice>()
            list.addAll(pairedDevices)

            rviDevicesPaired.setHasFixedSize(true)
            rviDevicesPaired.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
            pairedAdapter = PairedAdapter(this, list, macDevice)
            rviDevicesPaired.adapter = pairedAdapter
           /* val intent = Intent(this@MainActivity, DeviceListActivity::class.java)
            intent.putParcelableArrayListExtra("device.list", list)
            startActivity(intent)*/
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        /*if (btAdapter != null && btAdapter.isEnabled && !bluetoothDefaultIsEnable)
            btAdapter.disable()*/
    }

    override fun onPause() {
        if (mBtAdapter != null) {
            if (mBtAdapter.isDiscovering) {
                mBtAdapter.cancelDiscovery()
            }
        }
        super.onPause()


    }

    override fun onResume() {
        super.onResume()

    }




    override fun onBackPressed() {
        if (viewDialogDevicesNew.visibility == View.VISIBLE){
            viewDialogDevicesNew.visibility = View.GONE
            mBtAdapter.cancelDiscovery()
        }else{
            super.onBackPressed()
        }

    }


}
