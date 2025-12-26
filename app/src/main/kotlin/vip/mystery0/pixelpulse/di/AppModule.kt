package vip.mystery0.pixelpulse.di

import android.app.usage.NetworkStatsManager
import android.content.Context
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import vip.mystery0.pixelpulse.data.repository.NetworkRepository
import vip.mystery0.pixelpulse.data.source.impl.ShizukuSpeedDataSource
import vip.mystery0.pixelpulse.data.source.impl.StandardSpeedDataSource
import vip.mystery0.pixelpulse.service.NotificationHelper
import vip.mystery0.pixelpulse.ui.MainViewModel
import vip.mystery0.pixelpulse.ui.overlay.OverlayWindow

val appModule = module {
    single {
        androidContext().getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    }
    single { StandardSpeedDataSource(androidContext(), get()) }
    single { ShizukuSpeedDataSource() }

    single { NetworkRepository(get(), get()) }

    factory { NotificationHelper(androidContext()) }
    factory { OverlayWindow(androidContext()) }

    viewModel { MainViewModel(androidApplication(), get()) }
}
