package com.mdp.innovation.obd_driving.ui.activity

import android.content.Intent
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.control.ModuleVoltageCommand
import android.support.v4.app.FragmentManager
import com.mdp.innovation.obd_driving.R
import com.mdp.innovation.obd_driving.internal.CollectTripDataService
import com.mdp.innovation.obd_driving.ui.HomeView
import com.mdp.innovation.obd_driving.ui.fragment.CancelCollectDialogFragment
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.Global
import org.koin.android.ext.android.inject

class InitialActivity : BaseServiceActivity(){

    private val navigator by inject<Navigator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)

        navigator.navigateToLogin(supportFragmentManager, R.id.content)

    }

}
