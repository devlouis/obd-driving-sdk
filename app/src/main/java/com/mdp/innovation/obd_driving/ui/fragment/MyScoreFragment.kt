package com.mdp.innovation.obd_driving.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mdp.innovation.obd_driving.R
import android.support.v7.widget.Toolbar
import com.mdp.innovation.obd_driving.ui.MyScoreView
import com.mdp.innovation.obd_driving.util.Message
import kotlinx.android.synthetic.main.fragment_my_score.*
import android.support.annotation.Nullable
import com.mdp.innovation.obd_driving.interactor.MyScoreInteractor
import com.mdp.innovation.obd_driving.presenter.MyScorePresenter
import com.mdp.innovation.obd_driving.ui.activity.HomeActivity
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.CustomAnimate
import android.util.Log
import org.koin.android.ext.android.inject
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving_api.app.`interface`.ObdGatewayVin
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD


class MyScoreFragment : BaseFragment(), MyScoreView, ObdGatewayVin {
    val TAG =  javaClass.simpleName
    companion object {
        fun newInstance(): MyScoreFragment{
            return MyScoreFragment()
        }
    }

    var myActivity = HomeActivity()

    private val navigator by inject<Navigator>()

    private val presenter = MyScorePresenter(this, MyScoreInteractor())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_my_score, container, false)

        //var drawerLayout = activity?.findViewById(R.id.drawer_layout) as DrawerLayout

        var toolbar = view.findViewById(R.id.includeToolbar) as Toolbar

        toolbar.title = "Mi Calificación"
        toolbar.setNavigationIcon(R.drawable.ic_menu)

        myActivity = activity as HomeActivity
        myActivity.drawerConfig(toolbar)

        /*toolbar.setNavigationOnClickListener {
            System.out.println("Menuuuuuuuu")
            drawerLayout.openDrawer(Gravity.START)
        }*/

        return view
    }



    override fun onActivityCreated(@Nullable savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        myActivity = activity as HomeActivity
        initUI()

        if(myActivity.isActiveCollectDataService()){
            navigator.navigateToCollectTripData(activity, 1)
            Global.cancelValidated = false
        }
    }

    override fun getVin(vin: String){
        Log.i("[INFO]","ACTIVITY getVin: $vin")
        Global.myVIN = vin
        tv_vin.text = "Mi VIN es: " + vin
        ConnectOBD.stopLiveData()
        navigator.navigateToCollectTripData(activity, 1)
        Global.cancelValidated = false

    }
    override fun errorConnect(message: String){
        Log.i("[INFO]","ACTIVITY errorConnect: $message")
    }

    private fun initUI(){
        /*val toolbar = includeToolbar as Toolbar
        toolbar.title = "XXXXXXXXX"
        toolbar.setNavigationIcon(R.drawable.ic_bluetooth)*/

        //(activity as AppCompatActivity).setSupportActionBar(toolbar)

        //(activity as AppCompatActivity).supportActionBar?.title = "Title"

        CustomAnimate.setButtonAnimation(btnStartTrip)

        btnStartTrip.setOnClickListener {
            //nextActivity(PairObdActivity::class.java, true)
            //navigator.navigateToCollectData(fragmentManager, R.id.content)

            it.isEnabled = false
            it.postDelayed({
                presenter.isConnected()
                it.isEnabled = true
            }, 100L)
        }

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDeviceConnected(){
        //nextActivity(HomeActivity::class.java, false)
        //Message.toastShort("Preparando para iniciar el viaje.", activity?.applicationContext)
        goToStartTrip()
    }

    override fun onDeviceNoConnected(){
        (activity as HomeActivity).goToPairObd()
        Message.toastShort("Su OBD ha perdido la conexión. Por favor vuélvalo a conectar.", activity?.applicationContext)
    }

    override fun showProgress(){

    }
    override fun hideProgress(){

    }

    private fun goToStartTrip(){
        //myActivity.startCollectDataService()
        ConnectOBD.startLiveData(this)
        /*navigator.navigateToCollectTripData(activity, 1)
        Global.cancelValidated = false*/


    }

    fun showUpdateScoreProgressBar(){
        pb_update_score.visibility = View.VISIBLE
    }
}
