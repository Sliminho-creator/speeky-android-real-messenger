package com.speeky.app

import android.app.Application
import com.speeky.app.notifications.NotificationHelper

class SpeekyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
    }
}
