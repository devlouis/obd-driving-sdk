package com.mdp.innovation.obd_driving.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_collect_data.*
import android.support.annotation.Nullable
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.github.anastr.speedviewlib.PointerSpeedometer
import com.mdp.innovation.obd_driving.interactor.CollectDataInteractor
import com.mdp.innovation.obd_driving.interactor.MyScoreInteractor
import com.mdp.innovation.obd_driving.presenter.CollectDataPresenter
import com.mdp.innovation.obd_driving.presenter.MyScorePresenter
import com.mdp.innovation.obd_driving.service.model.UpdateVinResponse
import com.mdp.innovation.obd_driving.ui.CollectDataView
import com.mdp.innovation.obd_driving.ui.InterfaceView
import com.mdp.innovation.obd_driving.ui.activity.CollectTripDataActivity
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import com.mdp.innovation.obd_driving.util.CustomAnimate
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving.util.Message
import com.mdp.innovation.obd_driving.util.Preferences
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.core.service.LocationUpdatesService
import kotlinx.android.synthetic.main.activity_main_test.*
import org.koin.android.ext.android.inject


class CollectDataFragment : BaseServiceFragment(), CollectDataView, HomeActivity.StartLiveDataInterface {

    companion object {
        fun newInstance(): CollectDataFragment{
            return CollectDataFragment()
        }
    }

    private val presenter = CollectDataPresenter(this, CollectDataInteractor())

    //private val presenter = MyScorePresenter(this, MyScoreInteractor())

    private val preferences by inject<Preferences>()

    var runnable = Runnable {  }
    var handler = Handler(Looper.getMainLooper())


    var vinUpdated = false

    private lateinit var myReceiver: MyReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_collect_data, container, false)
        return view
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myReceiver = MyReceiver()
        initUI()
        (activity as HomeActivity).setOnStartLiveDataListener(this)
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(myReceiver)
        super.onPause()
        Global.appIsOpen = false
        (activity as HomeActivity).setOnStartLiveDataListener(null)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            myReceiver, IntentFilter(ConnectOBD.ACTION_BROADCAST)
        )

        Global.appIsOpen = true

        if(Global.isPendingToBack){
            fragmentManager?.popBackStack()
            Global.isPendingToBack = false
        }

        (activity as HomeActivity).setOnStartLiveDataListener(this)
    }

    private val customInterface = object : InterfaceView {
        override fun toDo() {
            Global.tripIsEnded = true
            //stopCollectDataService()
            MyScoreFragment.appFirstStart = false
            ConnectOBD.stopLiveData()
            fragmentManager?.popBackStack()
        }
    }

    private fun initUI(){
        val toolbar = includeToolbar as Toolbar
        toolbar.title = "Procesando..."
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)

        val myVIN = preferences.getVIN(context)

        //tv_vin.text = "Mi VIN es: $myVIN"

        toolbar.setNavigationOnClickListener {
            System.out.println("backkkk")

            //navigator.toPreviewFragment(activity?.supportFragmentManager)

            //activity?.onBackPressed()

            //validateCancel()

            //CollectTripDataActivity.validateCancel(fragmentManager!!)

            var dialog = CancelCollectDialogFragment.newInstance(customInterface)
            Global.cancelValidated = false
            dialog.show(fragmentManager,"cancel_collect")

        }

        CustomAnimate.setButtonAnimation(btnEndTrip)

        btnEndTrip.setOnClickListener {
            //nextActivity(PairObdActivity::class.java, true)
            vLoading.visibility = View.VISIBLE
            ConnectOBD.stopLiveData()

            it.isEnabled = false
            it.postDelayed({

                Global.cancelValidated = true
                Global.tripIsEnded = true
                //stopCollectDataService()
                MyScoreFragment.appFirstStart = false
                fragmentManager?.popBackStack()



                it.isEnabled = true
                vLoading.visibility = View.GONE
            }, 2000)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    /*fun validateCancel(){
        var dialog : CancelCollectDialogFragment = CancelCollectDialogFragment()
        dialog.show(fragmentManager,"cancel_collect_dialog_fragment")
    }*/

    /*override fun onDeviceConnected(){
        //nextActivity(HomeActivity::class.java, false)
        Message.toastShort("Preparando para iniciar el viaje.", activity?.applicationContext)
        goToStartTrip()
    }

    override fun onDeviceNoConnected(){
        (activity as HomeActivity).nextActivity(PairObdActivity::class.java, true)
        Message.toastShort("Su OBD ha perdido la conexión. Por favor vuélvalo a conectar.", activity?.applicationContext)
    }

    override fun showProgress(){

    }
    override fun hideProgress(){

    }*/

    override fun getVin(vin: String){

        if(!vinUpdated){

            var dataUser = preferences.getDataUser(context)

            val userId = dataUser!!.userId!!

            dataUser!!.vin = vin
            preferences.setDataUser(context,dataUser)

            presenter.updateVin(userId, vin)
        }

        vinUpdated = true

        /*Log.i("[INFO]","ACTIVITY getVin: $vin")
        activity!!.runOnUiThread {
            //hideProgress()

            Global.myVIN = vin
            preferences.setVIN(context,vin)
            //ConnectOBD.stopLiveData()
            //navigator.navigateToCollectData(fragmentManager, R.id.content)
            Global.cancelValidated = false
        }*/

    }

    override fun errorConnect(message: String){
        //hideProgress()
        //Message.toastLong(message,context)

        Log.i("[INFO]","ACTIVITY errorConnect: $message")
        if(activity != null){
            activity!!.runOnUiThread {
                //hideProgress()
                Message.toastLong(message,context)
            }
        }
    }

    override fun onUpdateVinSuccess(response: UpdateVinResponse) {

    }

    override fun onUpdateVinError(message: String) {

    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    /**
     * Receiver for broadcasts sent by [ConnectOBD].
     */
    private inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val extras = intent.extras
            val speed = extras.getString(ConnectOBD.EXTRA_SPEED)
            Log.v("CollDataFrag ", " getSpeedKm: onReceive  ${speed} km/h")
            speedometer.speedTo(speed.toFloat())

        }
    }
}
