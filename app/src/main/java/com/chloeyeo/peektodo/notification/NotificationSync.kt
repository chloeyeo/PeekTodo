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
        val locked: Boolean,
        val pinned: Boolean,
    )

    fun start() {
        if (job?.isActive == true) return
        TodoNotifier.ensureChannel(context)
        job = scope.launch {
            combine(
                todos.observeOpen(),
                settings.blurMode,
                lockState.isLocked,
                settings.pinNotification,
                refreshTicks,
            ) { open, blur, locked, pinned, _ -> Inputs(open, blur, locked, pinned) }
                .collect { (open, blur, locked, pinned) ->
                    TodoNotifier.show(context, open, blurMode = blur, locked = locked, pinned = pinned)
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
