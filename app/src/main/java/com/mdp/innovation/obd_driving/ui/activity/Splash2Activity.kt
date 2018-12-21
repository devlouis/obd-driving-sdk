package com.mdp.innovation.obd_driving.ui.activity

import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import kotlinx.android.synthetic.main.activity_splash.*
import com.mdp.innovation.obd_driving.R
import android.os.Looper
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import org.koin.android.ext.android.inject


class Splash2Activity : BaseAppCompat() {

    private val navigator by inject<Navigator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_splash)
        setContentView(R.layout.activity_splash)
        navigator.navigateToMain(this)
        finish()
    }

}
