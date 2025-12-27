package vip.mystery0.pixelpulse.di

import android.content.Context
import android.net.ConnectivityManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import vip.mystery0.pixelpulse.data.repository.NetworkRepository
import vip.mystery0.pixelpulse.data.source.impl.SpeedDataSource
import vip.mystery0.pixelpulse.service.NotificationHelper
import vip.mystery0.pixelpulse.ui.overlay.OverlayWindow

val appModule = module {
    single {
        androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    single {
        androidContext().getSharedPreferences("pixel_pulse_prefs", Context.MODE_PRIVATE)
    }
    single { SpeedDataSource(get()) }

    single { NetworkRepository(get(), get()) }

    factory { NotificationHelper(androidContext()) }
    factory { OverlayWindow(androidContext()) }
}
