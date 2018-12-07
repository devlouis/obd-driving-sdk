package com.mdp.innovation.obd_driving.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import kotlinx.android.synthetic.main.fragment_collect_data.*
import android.support.annotation.Nullable
import com.mdp.innovation.obd_driving.ui.activity.CollectTripDataActivity
import com.mdp.innovation.obd_driving.util.CustomAnimate
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD


class CollectDataFragment : BaseFragment() {

    companion object {
        fun newInstance(): CollectDataFragment{
            return CollectDataFragment()
        }
    }

    var myActivity = CollectTripDataActivity()

    //private val presenter = MyScorePresenter(this, MyScoreInteractor())

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

        tv_vin.text = "Mi VIN es: " + Global.myVIN

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
