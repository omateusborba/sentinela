package com.sentinela

import android.app.Application
import com.sentinela.di.AppContainer

class SentinelaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notificationHelper.createChannel()
    }
}
