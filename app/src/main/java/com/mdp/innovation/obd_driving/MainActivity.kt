package com.mdp.innovation.obd_driving

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.mdp.innovation.obd_driving_api.app.core.BaseAppCompat
import com.mdp.innovation.obd_driving_api.app.ui.PairObdActivity
import com.mdp.innovation.obd_driving_api.commands.ObdCommand
import com.mdp.innovation.obd_driving_api.commands.control.ModuleVoltageCommand
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseAppCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cmds = ArrayList<ObdCommand>()
        cmds.add(ModuleVoltageCommand())

        button.setOnClickListener {
            nextActivity(PairObdActivity::class.java, true)
        }

    }

}
