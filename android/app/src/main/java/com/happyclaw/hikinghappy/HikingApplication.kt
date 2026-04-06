package com.happyclaw.hikinghappy

import android.app.Application
import com.amap.api.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HikingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Amap privacy compliance — must be called before any SDK interface
        MapsInitializer.updatePrivacyShow(this, true, true)
        MapsInitializer.updatePrivacyAgree(this, true)
    }
}
