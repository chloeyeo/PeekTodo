package com.chloeyeo.peektodo

import android.content.Context
import com.chloeyeo.peektodo.data.SettingsRepository
import com.chloeyeo.peektodo.data.TodoDatabase
import com.chloeyeo.peektodo.data.TodoRepository
import com.chloeyeo.peektodo.notification.LockStateMonitor
import com.chloeyeo.peektodo.notification.NotificationSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Hand-rolled dependency container. Small enough that a DI framework would be
 * more ceremony than code; swap for Hilt if the graph grows.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    /** Lives as long as the process. Never cancelled. */
    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val database: TodoDatabase by lazy { TodoDatabase.build(appContext) }

    val todoRepository: TodoRepository by lazy { TodoRepository(database.todoDao()) }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(appContext) }

    val lockStateMonitor: LockStateMonitor by lazy { LockStateMonitor(appContext) }

    val notificationSync: NotificationSync by lazy {
        NotificationSync(appContext, todoRepository, settingsRepository, lockStateMonitor, applicationScope)
    }
}
