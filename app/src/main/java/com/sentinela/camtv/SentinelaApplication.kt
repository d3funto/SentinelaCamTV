package com.sentinela.camtv

import android.app.Application
import com.sentinela.camtv.config.defaultMosaicCameras
import com.sentinela.camtv.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class SentinelaApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Timber.plant(Timber.DebugTree())
        Timber.plant(container.fileTimberTree)
        container.crashReporter.install()

        if (BuildConfig.SEED_DEBUG_CAMERAS) {
            runBlocking(Dispatchers.IO) {
                container.cameraRepository.seedDebugCamerasIfEmpty(defaultMosaicCameras())
            }
        }
    }
}
