package com.rohitthebest.work_manager_codelabs

import android.app.Application
import androidx.viewbinding.BuildConfig
import timber.log.Timber

class BlurApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

    }
}