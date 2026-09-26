package com.chloeyeo.peektodo.notification

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tracks whether the keyguard is locked so blur mode can swap the
 * notification text on the lock screen only. Screen off counts as locked
 * (the keyguard engages then, possibly after a short delay); USER_PRESENT
 * marks the unlock.
 *
 * These broadcasts can only be received by a runtime-registered receiver,
 * so this works while the app process is alive. When the process is dead
 * the notification keeps whatever content it was last posted with, and
 * [android.app.Notification.publicVersion] still covers users who chose
 * "hide sensitive content" on the lock screen.
 */
class LockStateMonitor(private val context: Context) {

    private val keyguard: KeyguardManager
        get() = context.getSystemService(KeyguardManager::class.java)

    private val _isLocked = MutableStateFlow(keyguard.isKeyguardLocked)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            _isLocked.value = when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> true
                Intent.ACTION_USER_PRESENT -> false
                Intent.ACTION_SCREEN_ON -> keyguard.isKeyguardLocked
                else -> return
            }
        }
    }

    fun register() {
        if (registered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        // All three are protected system broadcasts; exporting exposes nothing.
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
        registered = true
    }
}
