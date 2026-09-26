package com.chloeyeo.peektodo

import android.app.Application

class PeekTodoApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Lock/unlock events drive the blur-mode text swap on the lock screen.
        container.lockStateMonitor.register()
        // Keeps the lock-screen notification in sync with the database for the
        // lifetime of the process. Every data change happens inside this process
        // (app or widget), so no service is needed.
        container.notificationSync.start()
    }
}
