package com.mdp.innovation.obd_driving_api.app.ui

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import java.util.ArrayList
import android.bluetooth.BluetoothDevice
import android.content.*
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mdp.innovation.obd_driving_api.app.ui.adapter.PairedAdapter
import com.mdp.innovation.obd_driving_api.app.utils.DevicePair
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import kotlinx.android.synthetic.main.activity_pair_obd.*


class PairObdActivity : BaseAppCompat() {
    val TAG = javaClass.simpleName
    val BLUETOOTH_LIST_KEY = "bluetooth_list_preference"

    var pairedNewAdapter: PairedAdapter? = null
    var pairedAdapter: PairedAdapter? = null
    val NewsDeviceStrings = ArrayList<BluetoothDevice>()

    var mBtAdapter: BluetoothAdapter? = null
    var mProgressDlg: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_obd)
        LogUtils().v(TAG, " Hola")
        initUI()
        onClickListener()

        //F8:CF:C5:59:54:EB

    }

    fun onClickListener(){
        llaBluetoothDiscovery.setOnClickListener {
            mBtAdapter!!.startDiscovery()
            viewDialogDevicesNew.visibility = View.VISIBLE

        }
        viewDialogDevicesNew.setOnClickListener {
            viewDialogDevicesNew.visibility = View.GONE
        }
        tviBluetoothStatus.setOnClickListener {
            if (!mBtAdapter!!.isEnabled){
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(intent, 1000)
            }

        }
    }

    fun BluetoohEnable(){
        tviBluetoothStatus.text = "Bluetooth conectado"
        tviBluetoothStatus.setTextColor(resources.getColor(R.color.new_verder))
        tviBluetoothStatus.isClickable = false
    }

    fun BluetoothDisabled(){
        tviBluetoothStatus.text = "Activar Bluetooth"
        tviBluetoothStatus.setTextColor(resources.getColor(R.color.error_color_material))
        tviBluetoothStatus.isClickable = true
    }

    fun initUI(){
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBtAdapter!!.isEnabled){
            BluetoohEnable()
        }else{
            BluetoothDisabled()
        }

        BluetoothPaired()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)

        rviDevicesNew.setHasFixedSize(true)
        rviDevicesNew.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        pairedNewAdapter = PairedAdapter(this, NewsDeviceStrings, false){
            LogUtils().v(TAG, " select: ${it.name} - ${it.address}")
            val device = it
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                unpairDevice(device)
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

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                    snackBarSucceso("Vinculado", claContent)
                } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    snackBarFail("Desvinculado", claContent)
                }
                //mAdapter.notifyDataSetChanged()
            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                /**
                 * muestra carga mientras busca
                 */
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
                if (device.bondState != BluetoothDevice.BOND_BONDED)
                    NewsDeviceStrings.add(device)
                rviDevicesNew.adapter = pairedNewAdapter
                //pairedAdapter!!.notifyDataSetChanged()

                LogUtils().v( "ConfigActivity" , " :: ${device.name}")

            }
        }
    }

    /**
     * Emparejar dispositivo
     */
    private fun pairDevice(device: BluetoothDevice) {
        try {
            val method = device.javaClass.getMethod("createBond", null as Class<*>)
            method.invoke(device, null as Array<Any>?)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    /**
     * Desemparejar dispositivo
     */
    private fun unpairDevice(device: BluetoothDevice) {
        try {
            val method = device.javaClass.getMethod("removeBond", null as Class<*>)
            method.invoke(device, null as Array<Any>?)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Lista de bluetooth emparejados
     */
    fun BluetoothPaired(){
        val pairedDevices = mBtAdapter!!.bondedDevices
        if (pairedDevices == null || pairedDevices.size == 0) {
            snackBarFail("No Paired Devices Found", claContent)
        } else {
            val list = ArrayList<BluetoothDevice>()
            list.addAll(pairedDevices)

            rviDevicesPaired.setHasFixedSize(true)
            rviDevicesPaired.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
            pairedAdapter = PairedAdapter(this, list, true){
                LogUtils().v(TAG, " select: ${it.name} - ${it.address}")

            }
            rviDevicesPaired.adapter = pairedAdapter
           /* val intent = Intent(this@MainActivity, DeviceListActivity::class.java)
            intent.putParcelableArrayListExtra("device.list", list)
            startActivity(intent)*/
        }
    }

}
