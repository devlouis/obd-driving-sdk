package com.mdp.innovation.obd_driving_api.app.ui.io

import android.content.Context

interface ObdProgressListener2 {
    fun stateUpdate(job: ObdCommandJob, context: Context)
}