package com.mdp.innovation.obd_driving_api_v2.app

import android.content.*
import android.os.IBinder
import android.widget.Toast
import com.mdp.innovation.obd_driving_api.R
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils
import com.mdp.innovation.obd_driving_api.data.store.SharedPreference
import com.mdp.innovation.obd_driving_api_v2.constants.DefineObdReader.ACTION_OBD_CONNECTION_STATUS
import com.mdp.innovation.obd_driving_api_v2.constants.DefineObdReader.ACTION_READ_OBD_REAL_TIME_DATA
import com.mdp.innovation.obd_driving_api_v2.service.LocationIntentService
import com.mdp.innovation.obd_driving_api_v2.service.ObdReaderService
import com.mdp.innovation.obd_driving_api_v2.trip.TripRecord


object ConnectOBDv2{

    val TAG = javaClass.simpleName
    val TAG_BD = " BD_LOCAL_V2"
    val TAG_GET = " TAG_GET"

    lateinit var mContext: Context

    lateinit var appSharedPreference: SharedPreference

    private var macDevice = ""
    var VIN = ""
    var mLocationUpdatesService = LocationIntentService()
    var mObdReaderService: ObdReaderService? = null

    var isServiceBoundOBD = false
    var isServiceBoundLocation = false


    init {
        LogUtils().v(TAG_BD, " Singleton class invoked. ")
    }

    fun initialize(context: Context) {
        mContext = context
        LogUtils().v(TAG_BD," mContext: ${mContext.getString(R.string.status_bluetooth_ok)}")

        appSharedPreference = SharedPreference(context)
        macDevice = appSharedPreference.getMacBluetooth()[appSharedPreference.MAC_DEVICE]!!
        VIN =  appSharedPreference.getVinCar()[appSharedPreference.VIN_CAR]!!

        LogUtils().v(TAG_BD, " macDevice:: $macDevice")

        //startLiveData()

    }

    private val mServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            //val binder = binder as LocationUpdatesService.LocalBinder
            /* mLocationUpdatesService = (binder as LocationUpdatesService.LocalBinder).intentService
             mLocationUpdatesService!!.requestLocationUpdates()*/
            LogUtils().v(TAG_BD, "onServiceConnected")
            val binder = service as ObdReaderService.LocalBinder
            mObdReaderService = binder.service
            isServiceBoundOBD = true

        }

        override fun onServiceDisconnected(className: ComponentName) {
            LogUtils().v(TAG_BD, className.toString() + " intentService is unbound")
            mObdReaderService = null
            isServiceBoundOBD = false
        }
    }
    private var serviceIntent = Intent()
    private var serviceIntentLocation = Intent()
    fun startLiveData(){
        isServiceBoundOBD = true
  /*      if (!isServiceBoundOBD){
            LogUtils().v(TAG_BD, " Binding OBD Service")
            mContext.bindService(Intent(mContext.applicationContext, ObdReaderService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
            //mContext.bindService(Intent(mContext.applicationContext, LocationIntentService::class.java), mServiceConnection, Context.BIND_AUTO_CREATE)
        }*/


        /**
         * Register receiver with some action related to OBD connection status
         */
        val intentFilter = IntentFilter()
        intentFilter.addAction(ACTION_READ_OBD_REAL_TIME_DATA)
        intentFilter.addAction(ACTION_OBD_CONNECTION_STATUS)
        //registerReceiver(mObdReaderReceiver, intentFilter)
        serviceIntent = Intent(mContext.applicationContext, ObdReaderService::class.java)
        mContext.startService(serviceIntent)
        serviceIntentLocation = Intent(mContext.applicationContext, LocationIntentService::class.java)
        mContext.startService(serviceIntentLocation)

/*        Handler().postDelayed({
            mLocationUpdatesService.requestLocationUpdates()
        }, 1000)*/

    }

    fun stopLiveData(){
        ObdReaderService.shouldContinue = false
        isServiceBoundOBD = false
        LocationIntentService().shouldContinueLoca = false
        //mContext.stopService(Intent(mContext.applicationContext, ObdReaderService::class.java))
        //mContext.stopService(Intent(mContext.applicationContext, LocationIntentService::class.java))
    }


    fun CheckConecction(): Boolean{
        return isServiceBoundOBD
    }

    /**
     * Broadcast Receiver to receive OBD connection status and real time data
     */
    private val mObdReaderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            //findViewById(R.id.progress_bar).setVisibility(View.GONE)
           // mObdInfoTextView.setVisibility(View.VISIBLE)
            val action = intent.action

            if (action == ACTION_OBD_CONNECTION_STATUS) {

                val connectionStatusMsg = intent.getStringExtra(ObdReaderService.INTENT_OBD_EXTRA_DATA)
                //mObdInfoTextView.setText(connectionStatusMsg)
                Toast.makeText(mContext, connectionStatusMsg, Toast.LENGTH_SHORT).show()

                if (connectionStatusMsg == mContext.getString(R.string.obd_connected)) {
                    //OBD connected  do what want after OBD connection
                } else if (connectionStatusMsg == mContext.getString(R.string.connect_lost)) {
                    //OBD disconnected  do what want after OBD disconnection
                } else {
                    // here you could check OBD connection and pairing status
                }

            } else if (action == ACTION_READ_OBD_REAL_TIME_DATA) {

                val tripRecord = TripRecord.getTripRecode(mContext)
                //mObdInfoTextView.setText(tripRecord.toString())
                // here you can fetch real time data from TripRecord using getter methods like
                //tripRecord.getSpeed();
                //tripRecord.getEngineRpm();
            }

        }
    }

}
