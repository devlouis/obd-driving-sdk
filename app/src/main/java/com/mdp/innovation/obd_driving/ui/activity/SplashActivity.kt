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


class SplashActivity : BaseAppCompat() {

    private val navigator by inject<Navigator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        initUI()
    }

    lateinit var runnable : Runnable
    lateinit var handler : Handler

    private fun initUI(){
        val animationTop = AnimationUtils.loadAnimation(applicationContext, R.anim.push_down_in)
        img_splash_top.startAnimation(animationTop)

        val animationFadeIn3000 = AnimationUtils.loadAnimation(applicationContext, R.anim.fade_in_3000)
        dcp.startAnimation(animationFadeIn3000)

        runnable = Runnable {
            lottie_loading.cancelAnimation()
            navigator.navigateToInitial(this)
            finish()
            //overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            //overridePendingTransition(R.anim.slidein, R.anim.slideout)
            //overridePendingTransition(R.anim.slideup,  R.anim.noanimation)
            //overridePendingTransition(R.anim.noanimation, R.anim.slidedown)

            //overridePendingTransition(R.anim.slideup,  R.anim.slidedown)

            //overridePendingTransition(R.anim.slidedown,  R.anim.slideup)
        }
        handler = Handler(Looper.getMainLooper())
        handler.postDelayed(runnable, 5000)
    }

    override fun onBackPressed() {
        if(handler != null) handler.removeCallbacks(runnable)
        finish()
    }

}
