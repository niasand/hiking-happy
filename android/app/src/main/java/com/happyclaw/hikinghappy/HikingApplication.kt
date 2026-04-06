package com.happyclaw.hikinghappy

import android.app.Application
import com.amap.api.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HikingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Set Amap API key from BuildConfig (loaded from local.properties)
        MapsInitializer.setApiKey(BuildConfig.AMAP_API_KEY)
    }
}
