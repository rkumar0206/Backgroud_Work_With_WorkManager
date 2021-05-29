package com.rohitthebest.work_manager_codelabs

import android.app.Application
import androidx.viewbinding.BuildConfig
import androidx.work.Configuration
import timber.log.Timber

class BlurApplication : Application(), Configuration.Provider {


    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

    }

    override fun getWorkManagerConfiguration(): Configuration {

        return if(BuildConfig.DEBUG) {

            Configuration.Builder().setMinimumLoggingLevel(android.util.Log.DEBUG).build()
        }else {

            Configuration.Builder().setMinimumLoggingLevel(android.util.Log.ERROR).build()
        }
    }
}