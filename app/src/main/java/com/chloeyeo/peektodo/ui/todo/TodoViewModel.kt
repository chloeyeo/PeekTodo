package com.chloeyeo.peektodo.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chloeyeo.peektodo.PeekTodoApp
import com.chloeyeo.peektodo.data.Todo
import com.chloeyeo.peektodo.data.TodoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoListUiState(
    val open: List<Todo> = emptyList(),
    val done: List<Todo> = emptyList(),
    /** False until the first database emission, so an empty list is not mistaken for "no data yet". */
    val loaded: Boolean = false,
) {
    val isEmpty: Boolean get() = open.isEmpty() && done.isEmpty()
}

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

    fun add(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.add(trimmed) }
    }

    fun setDone(todo: Todo, done: Boolean) {
        viewModelScope.launch { repository.setDone(todo.id, done) }
    }

    fun delete(todo: Todo) {
        viewModelScope.launch { repository.delete(todo) }
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
