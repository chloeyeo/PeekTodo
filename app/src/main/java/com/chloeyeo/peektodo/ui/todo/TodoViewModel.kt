package com.chloeyeo.peektodo.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chloeyeo.peektodo.PeekTodoApp
import com.chloeyeo.peektodo.data.Todo
import com.chloeyeo.peektodo.data.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TodoListUiState(
    val open: List<Todo> = emptyList(),
    val done: List<Todo> = emptyList(),
    /** False until the first database emission, so an empty list is not mistaken for "no data yet". */
    val loaded: Boolean = false,
) {
    val isEmpty: Boolean get() = open.isEmpty() && done.isEmpty()
}

/** The single row currently in edit mode, if any. */
data class EditState(
    val todoId: Long,
    /** Set when the user tried to save blank text; cleared on the next keystroke. */
    val showEmptyError: Boolean = false,
)

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    val uiState: StateFlow<TodoListUiState> = repository.observeAll()
        .map { todos ->
            val (done, open) = todos.partition { it.isDone }
            TodoListUiState(open = open, done = done, loaded = true)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodoListUiState(),
        )

    private val _edit = MutableStateFlow<EditState?>(null)
    /** Held here (not in the UI) so it survives rotation and only one row can be editing. */
    val edit: StateFlow<EditState?> = _edit.asStateFlow()

    fun add(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.add(trimmed) }
    }

    fun setDone(todo: Todo, done: Boolean) {
        viewModelScope.launch { repository.setDone(todo.id, done) }
    }

    fun delete(todo: Todo) {
        if (_edit.value?.todoId == todo.id) _edit.value = null
        viewModelScope.launch { repository.delete(todo) }
    }

    fun startEdit(todo: Todo) {
        _edit.value = EditState(todo.id)
    }

    fun cancelEdit() {
        _edit.value = null
    }

    fun clearEditError() {
        _edit.update { it?.copy(showEmptyError = false) }
    }

    /**
     * Saves the edited title. Blank text is rejected with an error and the
     * row stays in edit mode; the item is never deleted from here.
     */
    fun saveEdit(todo: Todo, title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) {
            _edit.update { it?.copy(showEmptyError = true) }
            return
        }
        _edit.value = null
        if (trimmed == todo.title) return
        // rename() copies the entity, so id, created_at and is_done are untouched.
        viewModelScope.launch { repository.rename(todo, trimmed) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PeekTodoApp
                TodoViewModel(app.container.todoRepository)
            }
        }
    }
}
