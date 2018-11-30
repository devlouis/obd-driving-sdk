package com.mdp.innovation.obd_driving_api.data.Broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mdp.innovation.obd_driving_api.app.ui.io.ObdGatewayService
import com.mdp.innovation.obd_driving_api.app.utils.LogUtils

class OBDRestarBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        LogUtils().v(" OBDRestar ", "Service Stop!")
        context.startService(Intent(context, ObdGatewayService::class.java))
    }

}