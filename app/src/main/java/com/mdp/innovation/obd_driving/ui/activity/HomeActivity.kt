package com.mdp.innovation.obd_driving.ui.activity

import android.Manifest
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_home.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.view.GravityCompat
import android.view.Gravity
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.internal.CollectTripDataService
import com.mdp.innovation.obd_driving.ui.HomeView
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.TextView
import com.mdp.innovation.obd_driving.model.DataUserModel
import com.mdp.innovation.obd_driving.ui.InterfaceView
import com.mdp.innovation.obd_driving.ui.fragment.*
import com.mdp.innovation.obd_driving.util.Global
import com.mdp.innovation.obd_driving.util.Preferences
import com.mdp.innovation.obd_driving_api.app.core.ConnectOBD
import com.mdp.innovation.obd_driving_api.app.ui.activity.PairObdActivity
import org.koin.android.ext.android.inject


class HomeActivity : BaseServiceActivity(), HomeView {

    private val TAG = javaClass.simpleName

    private val navigator by inject<Navigator>()
    private val preferences by inject<Preferences>()

    private var startLiveDataInterface: StartLiveDataInterface? = null

    //var sabe = SendDataIoTHub()
    lateinit var dataUser: DataUserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        dataUser = preferences.getDataUser(applicationContext)!!
        val name = dataUser!!.name

        val header = navigation_view.getHeaderView(0)
        val tv_welcome = header.findViewById<TextView>(R.id.tv_welcome)
        tv_welcome.text = "Hola $name,"

        setDrawerConfig()

        navigator.navigateToMyScore(supportFragmentManager, R.id.content)

        //ConnectOBD.getVInDummy()
        //sabe.InitClient()
        //Handler().post(mQueueCommands)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!checkPermission()){
                requestPermission()
            }
        }


        ConnectOBD.validateFailure()

    }

    fun startLiveData(){
        val connectionString = dataUser.connectionString
        ConnectOBD.startLiveData(dataUser.userId!!)
    }

    /*fun simulateSpeed(){
        var speed = 0F

        val fex: FloatArray = floatArrayOf(10f,20f,30f,40f,50f,60f,70f,80f,90f,100f,110f,120f,130f,140f,150f,160f,170f,180f,190f,200f)
        var index = 0
        var increase = true

        var runnable = Runnable {  }
        var handler = Handler(Looper.getMainLooper())
        runnable = Runnable {

            speed = fex[index]
            //startLiveDataInterface?.getSpeedKm(speed.toString())

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
    }*/

    fun setOnStartLiveDataListener(startLiveDataInterface: StartLiveDataInterface?){
        this.startLiveDataInterface = startLiveDataInterface
    }

    /*override fun getVin(vin: String){
        startLiveDataInterface?.getVin(vin)
    }*/

/*    override fun getSpeedKm(kmh: String) {
        startLiveDataInterface?.getSpeedKm(kmh)
    }*/

    override fun showProgress() {
        //progress.visibility = View.VISIBLE
    }
    override fun hideProgress() {
        //progress.visibility = View.GONE
    }

    private fun setDrawerConfig(){

        navigation_view.setNavigationItemSelectedListener{

            val fragmentCount = supportFragmentManager.backStackEntryCount
            Log.d(TAG, "getBackStackEntryCount in Menu: $fragmentCount")

            val currentFragment = supportFragmentManager.findFragmentById(R.id.content)

            when (it.itemId){
                R.id.action_1 -> {
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                    navigator.navigateToMyScore(supportFragmentManager, R.id.content)
                }
                R.id.action_2 -> {

                    if(currentFragment != null && currentFragment !is MyScoreFragment){
                        supportFragmentManager.beginTransaction().remove(currentFragment).commit()
                        supportFragmentManager.popBackStack()
                    }

                    navigator.navigateToMyTrips(supportFragmentManager, R.id.content)
                }
                R.id.action_3 -> {

                    if(currentFragment != null && currentFragment !is MyScoreFragment){
                        supportFragmentManager.beginTransaction().remove(currentFragment).commit()
                        supportFragmentManager.popBackStack()
                    }

                    navigator.navigateToConfiguration(supportFragmentManager, R.id.content)
                }
                R.id.action_4 -> {
                    var dialog = LogoutDialogFragment.newInstance()
                    dialog.show(supportFragmentManager,"logout")
                }
                /*R.id.action_3 -> toast("Opcion 3 clicked")
                R.id.action_4 ->{
                    toast("Opcion 4 clicked")
                }*/
            }
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }
    }

    fun goToPairObd(){
        nextActivity(PairObdActivity::class.java, true)
    }

    private val customInterface = object : InterfaceView {
        override fun toDo() {
            stopCollectDataService()
            supportFragmentManager?.popBackStack()
        }
    }

    override fun onBackPressed() {

        val fragmentCount = supportFragmentManager.backStackEntryCount
        Log.d(TAG, "getBackStackEntryCount in BackPressed: $fragmentCount")


        /*if(fragmentCount == 0){
            if(drawer_layout.isDrawerOpen(Gravity.START)) drawer_layout.closeDrawer(GravityCompat.START)
            else super.onBackPressed()
        }else if(fragmentCount == 1){
            //System.out.println(Global.cancelValidated)
            //if(Global.cancelValidated) supportFragmentManager.popBackStack()
            //else CollectTripDataActivity.validateCancel(supportFragmentManager)

            val fragmentName = supportFragmentManager.getBackStackEntryAt(0).name
            if(fragmentName == "collect_data"){
                var dialog = CancelCollectDialogFragment.newInstance(customInterface)
                Global.cancelValidated = false
                dialog.show(supportFragmentManager,"cancel_collect")
            }else{
                super.onBackPressed()
            }

        }else{
            super.onBackPressed()
        }*/


        if(drawer_layout.isDrawerOpen(Gravity.START)) drawer_layout.closeDrawer(GravityCompat.START)
        else{

            val currentFragment = supportFragmentManager.findFragmentById(R.id.content)
            if(currentFragment != null && currentFragment is CollectDataFragment){
                var dialog = CancelCollectDialogFragment.newInstance(customInterface)
                Global.cancelValidated = false
                dialog.show(supportFragmentManager,"cancel_collect")
            }else{
                super.onBackPressed()
            }

        }

    }

    /*fun drawerConfig(toolbar : Toolbar){
        var drawerLayout = this.findViewById(R.id.drawer_layout) as DrawerLayout
        toolbar.setNavigationOnClickListener {
            System.out.println("Menuuuuuuuu")
            drawerLayout.openDrawer(Gravity.START)
        }
    }*/

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ActivityCompat.requestPermissions(this, permissions,0)
    }

    /*private val mQueueCommands = object : Runnable {
        override fun run() {

            //sabe.sendData2()
            //Handler().postDelayed(this, 500)
        }
    }*/

    interface StartLiveDataInterface {
        //fun getVin(vin: String)
    }


    override fun onStop() {
        if (ConnectOBD.isServiceBoundLocation) {
            // Desconectarse del servicio.
            // Esto le indica al servicio que esta actividad ya no está en primer plano
            // y que el servicio puede responder promoviéndose a sí mismo a un servicio en primer plano.
            ConnectOBD.doUnbindServiceLocation()
        }
        super.onStop()
    }

}
