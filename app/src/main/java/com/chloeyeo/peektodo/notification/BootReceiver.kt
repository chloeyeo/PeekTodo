package com.chloeyeo.peektodo.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chloeyeo.peektodo.PeekTodoApp
import kotlinx.coroutines.launch

/**
 * Notifications do not survive a reboot, so re-post the open-todo notification
 * once the device is back up.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
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
