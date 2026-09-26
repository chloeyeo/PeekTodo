package com.chloeyeo.peektodo.ui.todo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chloeyeo.peektodo.R
import com.chloeyeo.peektodo.data.Todo
import com.chloeyeo.peektodo.ui.reveal.RevealText

@Composable
fun TodoListRoute(
    revealKey: Int,
    onOpenSettings: () -> Unit,
    viewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TodoListScreen(
        state = state,
        revealKey = revealKey,
        onAdd = viewModel::add,
        onSetDone = viewModel::setDone,
        onDelete = viewModel::delete,
        onOpenSettings = onOpenSettings,
    )
}

/**
 * @param revealKey 0 renders normally. Any other value plays the staggered
 * reveal on the open items once the list has loaded, and re-plays when it
 * changes. This is the fallback for blur mode when the full-screen-intent
 * permission is missing: the notification tap opens here instead.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    state: TodoListUiState,
    revealKey: Int,
    onAdd: (String) -> Unit,
    onSetDone: (Todo, Boolean) -> Unit,
    onDelete: (Todo) -> Unit,
    onOpenSettings: () -> Unit,
) {
    var revealed by remember(revealKey) { mutableStateOf(revealKey == 0) }
    LaunchedEffect(revealKey, state.loaded) {
        if (revealKey != 0 && state.loaded && !revealed) {
            // Let the masked rows draw one frame so the animation has a start state.
            withFrameNanos { }
            revealed = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.todo_list_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AddTodoRow(
                onAdd = onAdd,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (state.loaded && state.isEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.todo_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    itemsIndexed(state.open) { index, todo ->
                        TodoRow(
                            todo = todo,
                            revealIndex = index,
                            revealed = revealed,
                            onSetDone = onSetDone,
                            onDelete = onDelete,
                        )
                    }
                    if (state.done.isNotEmpty()) {
                        item(key = "done-header") {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Text(
                                text = stringResource(R.string.todo_section_done),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                        items(state.done, key = { it.id }) { todo ->
                            TodoRow(
                                todo = todo,
                                revealIndex = 0,
                                revealed = true,
                                onSetDone = onSetDone,
                                onDelete = onDelete,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexed(
    todos: List<Todo>,
    content: @Composable (index: Int, todo: Todo) -> Unit,
) {
    items(todos.size, key = { todos[it].id }) { index -> content(index, todos[index]) }
}

@Composable
private fun AddTodoRow(
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (text.isNotBlank()) {
            onAdd(text)
            text = ""
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(stringResource(R.string.todo_input_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.weight(1f),
        )
        FilledIconButton(
            onClick = submit,
            enabled = text.isNotBlank(),
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.todo_add))
        }
    }
}

@Composable
private fun TodoRow(
    todo: Todo,
    revealIndex: Int,
    revealed: Boolean,
    onSetDone: (Todo, Boolean) -> Unit,
    onDelete: (Todo) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = todo.isDone,
            onCheckedChange = { onSetDone(todo, it) },
        )
        RevealText(
            text = todo.title,
            index = revealIndex,
            revealed = revealed,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
            color = if (todo.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onDelete(todo) }) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.todo_delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
