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
import com.mdp.innovation.obd_driving_api.app.ui.adapter.PairedAdapter
import com.mdp.innovation.obd_driving_api.app.ui.config.ObdConfig
import com.mdp.innovation.obd_driving_api.app.ui.io.*
import com.mdp.innovation.obd_driving_api.app.utils.DevicePair
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.commands.SpeedCommand
import com.mdp.innovation.obd_driving_api.commands.control.VinCommand
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference
import com.mdp.innovation.obd_driving_api.enums.AvailableCommandNames
import kotlinx.android.synthetic.main.activity_pair_obd.*
import roboguice.RoboGuice
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PairObdActivity : BaseAppCompat(), ObdProgressListener {

    /**
     * Actualización del estado
     */
    override fun stateUpdate(job: ObdCommandJob) {
        val cmdName = job.command.name
        val cmdID = LookUpCommand(cmdName)
        snackBarSucceso(cmdID, claContent)

        updateTripStatistic(job, cmdID = cmdID)
    }

    /**
     * Comando de búsqueda
     */
    fun LookUpCommand(txt: String): String {
        for (item in AvailableCommandNames.values()) {
            if (item.value == txt) return item.name
        }
        return txt
    }

    private fun queueCommands() {
        if (isServiceBound) {
            for (Command in ObdConfig.getCommands()) {
                //if (prefs.getBoolean(Command.name, true))
                    service!!.queueJob(ObdCommandJob(Command))
            }
        }
    }

    private val mQueueCommands = object : Runnable {
        override fun run() {
            if (service != null && service!!.isRunning && service!!.queueEmpty()) {
                queueCommands()

            }
            // run again in period defined in preferences
            Handler().postDelayed(this, 4000)
        }
    }

    private fun updateTripStatistic(job: ObdCommandJob, cmdID: String) {
        //Log.v(TAG, " updateTripStatistic ")
        //Toast.makeText(this, "updateTripStatistic", Toast.LENGTH_LONG).show()
        if (cmdID == AvailableCommandNames.SPEED.toString()) run {
            val command = job.command as SpeedCommand
            Log.v(TAG, " Speed" + command.metricSpeed)
            //Toast.makeText(this, " Speed: " + command.metricSpeed, Toast.LENGTH_LONG).show()
            //currentTrip.setSpeedMax(command.getMetricSpeed())
        }
        if (cmdID == AvailableCommandNames.VIN.toString()) run {
            if (VIN.isEmpty()) {
                val command = job.command as VinCommand
                Log.v(TAG, " VIN " + command.formattedResult)
                VIN = command.formattedResult
                Toast.makeText(this, " VIN: $VIN", Toast.LENGTH_LONG).show()
                //currentTrip.setSpeedMax(command.getMetricSpeed())
            }else{
                if (isServiceBound){
                    doUnbindService()
                }
                Toast.makeText(this, " VAR VIN: $VIN", Toast.LENGTH_LONG).show()
            }
        }
    }


    val TAG = javaClass.simpleName
    val BLUETOOTH_LIST_KEY = "bluetooth_list_preference"
    var VIN = ""

    var pairedNewAdapter: PairedAdapter? = null
    var pairedAdapter: PairedAdapter? = null
    var NewsDeviceStrings = ArrayList<BluetoothDevice>()

    var mBtAdapter: BluetoothAdapter? = null
    var mProgressDlg: ProgressDialog? = null

    lateinit var appSharedPreference: SharedPreference

    private var service: AbstractGatewayService? = null
    private var bluetoothDefaultIsEnable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_obd)
        RoboGuice.setUseAnnotationDatabases(false)
        LogUtils().v(TAG, " Hola")
        initUI()
        onClickListener()

        //F8:CF:C5:59:54:EB

    }

    fun onClickListener(){
        llaBluetoothDiscovery.setOnClickListener {
           /* mBtAdapter!!.startDiscovery()
            viewDialogDevicesNew.visibility = View.VISIBLE*/
            startLiveData()

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

        //Validar mac existente.
        appSharedPreference = SharedPreference(this)
        var macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        LogUtils().v(TAG, " macDevice:: $macDevice")

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null)
            bluetoothDefaultIsEnable = btAdapter!!.isEnabled

        mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBtAdapter!!.isEnabled){
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
        pairedNewAdapter = PairedAdapter(this, NewsDeviceStrings, false, -1) { bluetoothDevice: BluetoothDevice, pos: Int ->
            LogUtils().v(TAG, " select: ${bluetoothDevice.name} - ${bluetoothDevice.address}")
            LogUtils().v(TAG, " POS: ${pos}")
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
                if (device.bondState != BluetoothDevice.BOND_BONDED)
                    NewsDeviceStrings.add(device)
                rviDevicesNew.adapter = pairedNewAdapter
                //pairedAdapter!!.notifyDataSetChanged()

                LogUtils().v( "ConfigActivity" , " :: ${device.name}")

            }
        }
    }

    /**
     * Lista de bluetooth emparejados
     */
    var posPaiered = -1
    fun BluetoothPaired(){
        val pairedDevices = mBtAdapter!!.bondedDevices
        if (pairedDevices == null || pairedDevices.size == 0) {
            snackBarFail("No Paired Devices Found", claContent)
        } else {
            val list = ArrayList<BluetoothDevice>()
            list.addAll(pairedDevices)

            rviDevicesPaired.setHasFixedSize(true)
            rviDevicesPaired.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
            pairedAdapter = PairedAdapter(this, list, true, posPaiered) { bluetoothDevice: BluetoothDevice, pos: Int ->
                LogUtils().v(TAG, " select: ${bluetoothDevice.name} - ${bluetoothDevice.address}")
                LogUtils().v(TAG, " POS: ${pos + 1}")
                posPaiered = (pos + 1)
                appSharedPreference.saveMacBluetooth(bluetoothDevice.address)
                BluetoothPaired()


            }
            rviDevicesPaired.adapter = pairedAdapter
           /* val intent = Intent(this@MainActivity, DeviceListActivity::class.java)
            intent.putParcelableArrayListExtra("device.list", list)
            startActivity(intent)*/
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)

        if (isServiceBound){
            doUnbindService()
        }

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        if (btAdapter != null && btAdapter.isEnabled && !bluetoothDefaultIsEnable)
            btAdapter.disable()
    }

    override fun onPause() {
        if (mBtAdapter != null) {
            if (mBtAdapter!!.isDiscovering) {
                mBtAdapter!!.cancelDiscovery()
            }
        }
        super.onPause()


    }

    override fun onResume() {
        super.onResume()
        // get Bluetooth device
        val btAdapter = BluetoothAdapter
            .getDefaultAdapter()

        //preRequisites = btAdapter != null && btAdapter.isEnabled
        //preRequisites = btAdapter != null && btAdapter.enable()
        /*  if (!preRequisites && prefs.getBoolean(ConfigActivity.ENABLE_BT_KEY, false)) {
              preRequisites = btAdapter != null && btAdapter.enable()
          }*/
    }


    private var isServiceBound: Boolean = false
    private var preRequisites = true
    private val serviceConn = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, binder: IBinder) {
            Log.d(TAG, className.toString() + " service is bound")
            isServiceBound = true
            service = (binder as AbstractGatewayService.AbstractGatewayServiceBinder).service
            service!!.setContext(this@PairObdActivity)
            Log.d(TAG, "Starting live data")
            try {
                service!!.startService()
                if (preRequisites)
                    btStatusTextView.text = getString(R.string.status_bluetooth_connected)
            } catch (ioe: IOException) {
                Log.e(TAG, "Failure Starting live data")
                btStatusTextView.text = getString(R.string.status_bluetooth_error_connecting)
                doUnbindService()
            }

        }


        // This method is *only* called when the connection to the service is lost unexpectedly
        // and *not* when the client unbinds (http://developer.android.com/guide/components/bound-services.html)
        // So the isServiceBound attribute should also be set to false when we unbind from the service.
        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG, className.toString() + " service is unbound")
            isServiceBound = false
        }
    }

    fun startLiveData() {
        Log.d(TAG, "Starting live data..")


        doBindService()

        // start command execution
        Handler().post(mQueueCommands)
    }

    private fun doBindService() {
        if (!isServiceBound) {
            Log.d(TAG, "Binding OBD service..")
            if (preRequisites) {
                btStatusTextView.text = getString(R.string.status_bluetooth_connecting)
                val serviceIntent = Intent(applicationContext, ObdGatewayService::class.java)
                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE)
            } else {
                btStatusTextView.text = getString(R.string.status_bluetooth_disabled)
                val serviceIntent = Intent(applicationContext, MockObdGatewayService::class.java)
                bindService(serviceIntent, serviceConn, Context.BIND_AUTO_CREATE)
            }
        }
    }

    private fun doUnbindService() {
        if (isServiceBound) {
            if (service!!.isRunning) {
                service!!.stopService()
                if (preRequisites)
                    btStatusTextView.text = getString(R.string.status_bluetooth_ok)
            }
            Log.d(TAG, "Unbinding OBD service..")
            unbindService(serviceConn)
            isServiceBound = false

            //OBD status
            //obdStatusTextView.setText(getString(R.string.status_obd_disconnected))
        }
    }


}
