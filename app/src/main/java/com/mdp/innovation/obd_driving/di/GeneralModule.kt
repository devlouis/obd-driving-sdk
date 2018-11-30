package com.mdp.innovation.obd_driving.di

import com.mdp.innovation.obd_driving.ui.navigation.Navigator
import org.koin.dsl.module.module

val generalModule = module {
    single { Navigator() }
}