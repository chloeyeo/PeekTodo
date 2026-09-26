package com.chloeyeo.peektodo.notification

import android.content.Context
import com.chloeyeo.peektodo.data.SettingsRepository
import com.chloeyeo.peektodo.data.Todo
import com.chloeyeo.peektodo.data.TodoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Observes open to-dos, the blur and pin settings and the lock state for the
 * life of the process and re-posts the persistent notification whenever any
 * of them changes. Editing a title, toggling a setting, or locking the device
 * all flow through here.
 *
 * Lock-state changes are special: they exist only so blur mode can swap the
 * text on the lock screen, so they may update a notification that is showing
 * but must never bring back one the user swiped away (pin mode off). Pin mode
 * on re-posts through [NotificationDismissedReceiver] instead.
 */
class NotificationSync(
    private val context: Context,
    private val todos: TodoRepository,
    private val settings: SettingsRepository,
    private val lockState: LockStateMonitor,
    private val scope: CoroutineScope,
) {
    private var job: Job? = null

    /** Bumped to force a re-post (e.g. right after the user grants notification permission). */
    private val refreshTicks = MutableStateFlow(0)

    private data class Inputs(
        val open: List<Todo>,
        val blur: Boolean,
        val pinned: Boolean,
        val locked: Boolean,
        val tick: Int,
    )

    fun start() {
        if (job?.isActive == true) return
        TodoNotifier.ensureChannel(context)
        job = scope.launch {
            var previous: Inputs? = null
            combine(
                todos.observeOpen(),
                settings.blurMode,
                settings.pinNotification,
                lockState.isLocked,
                refreshTicks,
            ) { open, blur, pinned, locked, tick -> Inputs(open, blur, pinned, locked, tick) }
                .collect { inputs ->
                    val onlyLockChanged = previous != null && inputs.copy(locked = previous!!.locked) == previous
                    previous = inputs
                    if (onlyLockChanged) {
                        // Nothing to swap when blur is off, and never resurrect a dismissed one.
                        if (!inputs.blur || !TodoNotifier.isShowing(context)) return@collect
                    }
                    TodoNotifier.show(
                        context,
                        inputs.open,
                        blurMode = inputs.blur,
                        locked = inputs.locked,
                        pinned = inputs.pinned,
                    )
                }
        }
    }

    fun refresh() {
        refreshTicks.update { it + 1 }
    }

    /**
     * One-shot post from the current state. Used by the broadcast receivers
     * (boot, app update, notification dismissed), which may run in a process
     * that has only just been created.
     */
    suspend fun postOnce() {
        TodoNotifier.ensureChannel(context)
        TodoNotifier.show(
            context,
            todos.getOpen(),
            blurMode = settings.blurMode.first(),
            locked = lockState.isLocked.value,
            pinned = settings.pinNotification.first(),
        )
    }
}
