package com.chloeyeo.peektodo.notification

import android.content.Context
import com.chloeyeo.peektodo.data.SettingsRepository
import com.chloeyeo.peektodo.data.TodoRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Observes open to-dos, the blur setting and the lock state for the life of
 * the process and re-posts the persistent notification whenever any changes.
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

    fun start() {
        if (job?.isActive == true) return
        TodoNotifier.ensureChannel(context)
        job = scope.launch {
            combine(
                todos.observeOpen(),
                settings.blurMode,
                lockState.isLocked,
                refreshTicks,
            ) { open, blur, locked, _ -> Triple(open, blur, locked) }
                .collect { (open, blur, locked) ->
                    TodoNotifier.show(context, open, blurMode = blur, locked = locked)
                }
        }
    }

    fun refresh() {
        refreshTicks.update { it + 1 }
    }

    /** One-shot post from the current state. Used by [BootReceiver]. */
    suspend fun postOnce() {
        TodoNotifier.ensureChannel(context)
        TodoNotifier.show(
            context,
            todos.getOpen(),
            blurMode = settings.blurMode.first(),
            locked = lockState.isLocked.value,
        )
    }
}
