package com.mdp.innovation.obd_driving.ui.fragment

import android.content.Context
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
import com.github.anastr.speedviewlib.PointerSpeedometer
import com.mdp.innovation.obd_driving.ui.activity.CollectTripDataActivity
import com.mdp.innovation.obd_driving.util.CustomAnimate
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving.util.Preferences
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import org.koin.android.ext.android.inject


class CollectDataFragment : BaseFragment() {

    companion object {
        fun newInstance(): CollectDataFragment{
            return CollectDataFragment()
        }
    }

    var myActivity = CollectTripDataActivity()

    //private val presenter = MyScorePresenter(this, MyScoreInteractor())

    private val preferences by inject<Preferences>()

    var runnable = Runnable {  }
    var handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_collect_data, container, false)
        return view
    }

    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myActivity = activity as CollectTripDataActivity
        initUI()
    }

    private fun initUI(){
        val toolbar = includeToolbar as Toolbar
        toolbar.title = "Procesando..."
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)

        val myVIN = preferences.getVIN(context)

        tv_vin.text = "Mi VIN es: $myVIN"

        toolbar.setNavigationOnClickListener {
            System.out.println("backkkk")

            //navigator.toPreviewFragment(activity?.supportFragmentManager)

            //activity?.onBackPressed()

            //validateCancel()
            CollectTripDataActivity.validateCancel(fragmentManager!!)

        }

        CustomAnimate.setButtonAnimation(btnEndTrip)

        btnEndTrip.setOnClickListener {
            //nextActivity(PairObdActivity::class.java, true)
            it.isEnabled = false
            it.postDelayed({

                Global.cancelValidated = true
                myActivity.stopCollectDataService()
                //ConnectOBD.stopLiveData()


                it.isEnabled = true
            }, 100L)
        }

        var speed = 0F

        speedometer.speedTo(speed)

        val fex: FloatArray = floatArrayOf(10f,20f,30f,40f,50f,60f,70f,80f,90f,100f,110f,120f,130f,140f,150f,160f,170f,180f,190f,200f)
        var index = 0
        var increase = true

        runnable = Runnable {  }
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {

            speed = fex[index]
            speedometer.speedTo(speed)

            /*if(index == 0) increase = true
            else if(index == fex.size - 1) increase = false
            else{
                var rand = (0..1).random()
                increase = (rand == 1)
            }*/

            increase = when(index){
                0 -> true
                fex.size - 1 -> false
                else -> {
                    var rand = (0..1).random()
                    (rand == 1)
                }
            }

            if(increase) index+=1
            else index-=1

            handler.postDelayed(runnable, 2000)

        }
        handler.postDelayed(runnable, 2000)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    /*fun validateCancel(){
        var dialog : CancelCollectDialogFragment = CancelCollectDialogFragment()
        dialog.show(fragmentManager,"cancel_collect_dialog_fragment")
    }*/

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

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

    private fun goToStartTrip(){

    }
}
