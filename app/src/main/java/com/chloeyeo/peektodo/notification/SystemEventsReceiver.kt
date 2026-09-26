package com.chloeyeo.peektodo.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chloeyeo.peektodo.PeekTodoApp
import kotlinx.coroutines.launch

/**
 * Notifications do not survive a reboot or an app update, so re-post the
 * open-todo notification after either. This runs regardless of pin mode: the
 * notification is the app's whole purpose, and pin mode only decides whether
 * the user can swipe it away afterwards.
 */
class SystemEventsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> Unit
            else -> return
        }
        val app = context.applicationContext as PeekTodoApp
        val pending = goAsync()
        app.container.applicationScope.launch {
            try {
                app.container.notificationSync.postOnce()
            } finally {
                pending.finish()
            }
        }
    }
}
