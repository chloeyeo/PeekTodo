package com.chloeyeo.peektodo.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chloeyeo.peektodo.PeekTodoApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Target of the notification's deleteIntent, which the system fires when the
 * user swipes the notification away or taps "Clear all". The deleteIntent is
 * only attached while pin mode is on, and the setting is re-checked here in
 * case it was turned off between posting and dismissal.
 */
class NotificationDismissedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as PeekTodoApp
        val pending = goAsync()
        app.container.applicationScope.launch {
            try {
                if (app.container.settingsRepository.pinNotification.first()) {
                    app.container.notificationSync.postOnce()
                }
            } finally {
                pending.finish()
            }
        }
    }
}
