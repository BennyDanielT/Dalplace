package com.example.dalplace

import android.app.Application
import com.onesignal.OneSignal

/**
 * Source - Official Docs - https://onesignal.com/blog/how-to-add-push-notifications-to-an-android-app-with-java-and-kotlin/
 * Author - Official docs
 * Reason for using - Setting up the in-app notification
 */
class OneSignalApplication : Application() {
    private val ONESIGNAL_APP_ID = getString(R.string.one_signal_app_id)

    override fun onCreate() {
        super.onCreate()

        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)

        OneSignal.initWithContext(this)
        OneSignal.setAppId(ONESIGNAL_APP_ID)
    }
}