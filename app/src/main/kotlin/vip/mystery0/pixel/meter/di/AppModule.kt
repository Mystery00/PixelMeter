package vip.mystery0.pixel.meter.di

import android.content.Context
import android.net.ConnectivityManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import vip.mystery0.pixel.meter.data.repository.DataStoreRepository
import vip.mystery0.pixel.meter.data.repository.NetworkRepository
import vip.mystery0.pixel.meter.data.repository.dataStore
import vip.mystery0.pixel.meter.data.source.impl.SpeedDataSource
import vip.mystery0.pixel.meter.service.NotificationHelper
import vip.mystery0.pixel.meter.ui.overlay.OverlayWindow

val appModule = module {
    single {
        androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    single { DataStoreRepository(androidContext().dataStore) }
    single { SpeedDataSource(get()) }

    single { NetworkRepository(get(), get()) }

    factory { NotificationHelper(androidContext()) }
    factory { OverlayWindow(androidContext(), get()) }
}
