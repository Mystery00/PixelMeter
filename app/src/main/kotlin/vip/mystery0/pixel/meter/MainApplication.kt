package vip.mystery0.pixel.meter

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import vip.mystery0.pixel.meter.di.appModule
import vip.mystery0.pixel.meter.service.NotificationHelper

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }

        // Initialize Notification Channel immediately
        NotificationHelper.createNotificationChannel(this)
    }
}
