package com.mdp.innovation.obd_driving.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent



class ServiceRestarterBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(context, CollectTripDataService::class.java))
    }

}