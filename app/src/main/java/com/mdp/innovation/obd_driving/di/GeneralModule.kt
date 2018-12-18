package com.mdp.innovation.obd_driving.di

import com.mdp.innovation.obd_driving.service.WSService
import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import com.mdp.innovation.obd_driving.util.Preferences
import org.koin.dsl.module.module

val generalModule = module {
    single { Navigator() }
    single { Preferences() }
    single { WSService() }
}