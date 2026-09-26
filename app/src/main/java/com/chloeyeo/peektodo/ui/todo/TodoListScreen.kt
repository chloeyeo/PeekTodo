package com.chloeyeo.peektodo.ui.todo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chloeyeo.peektodo.R
import com.chloeyeo.peektodo.data.Todo
import com.chloeyeo.peektodo.ui.components.ClipboardCard
import com.chloeyeo.peektodo.ui.components.ShibaCheckbox
import com.chloeyeo.peektodo.ui.reveal.RevealText
import com.chloeyeo.peektodo.ui.theme.shiba

@Composable
fun TodoListRoute(
    revealKey: Int,
    onOpenSettings: () -> Unit,
    viewModel: TodoViewModel = viewModel(factory = TodoViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val edit by viewModel.edit.collectAsStateWithLifecycle()
    TodoListScreen(
        state = state,
        edit = edit,
        revealKey = revealKey,
        onAdd = viewModel::add,
        onSetDone = viewModel::setDone,
        onDelete = viewModel::delete,
        onStartEdit = viewModel::startEdit,
        onSaveEdit = viewModel::saveEdit,
        onCancelEdit = viewModel::cancelEdit,
        onEditTextChanged = viewModel::clearEditError,
        onOpenSettings = onOpenSettings,
    )
}

/**
 * @param revealKey 0 renders normally. Any other value plays the staggered
 * reveal on the open items once the list has loaded, and re-plays when it
 * changes (a notification tap in blur mode).
 * @param edit the row currently being edited, or null.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    state: TodoListUiState,
    edit: EditState?,
    revealKey: Int,
    onAdd: (String) -> Unit,
    onSetDone: (Todo, Boolean) -> Unit,
    onDelete: (Todo) -> Unit,
    onStartEdit: (Todo) -> Unit,
    onSaveEdit: (Todo, String) -> Unit,
    onCancelEdit: () -> Unit,
    onEditTextChanged: () -> Unit,
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

    // Back while editing discards the edit instead of leaving the screen.
    BackHandler(enabled = edit != null, onBack = onCancelEdit)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.todo_list_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
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
                EmptyState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                ) {
                    itemsIndexed(state.open) { index, todo ->
                        TodoRow(
                            todo = todo,
                            edit = edit?.takeIf { it.todoId == todo.id },
                            revealIndex = index,
                            revealed = revealed,
                            onSetDone = onSetDone,
                            onDelete = onDelete,
                            onStartEdit = onStartEdit,
                            onSaveEdit = onSaveEdit,
                            onCancelEdit = onCancelEdit,
                            onEditTextChanged = onEditTextChanged,
                        )
                    }
                    if (state.done.isNotEmpty()) {
                        item(key = "done-header") {
                            Text(
                                text = stringResource(R.string.todo_section_done),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 8.dp),
                            )
                        }
                        items(state.done, key = { it.id }) { todo ->
                            TodoRow(
                                todo = todo,
                                edit = edit?.takeIf { it.todoId == todo.id },
                                revealIndex = 0,
                                revealed = true,
                                onSetDone = onSetDone,
                                onDelete = onDelete,
                                onStartEdit = onStartEdit,
                                onSaveEdit = onSaveEdit,
                                onCancelEdit = onCancelEdit,
                                onEditTextChanged = onEditTextChanged,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun LazyListScope.itemsIndexed(
    todos: List<Todo>,
    content: @Composable (index: Int, todo: Todo) -> Unit,
) {
    items(todos.size, key = { todos[it].id }) { index -> content(index, todos[index]) }
}

/** The shiba from the launcher icon, peeking over an empty clipboard. */
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        // The adaptive-icon foreground keeps its 108 dp canvas padding, so it
        // is drawn large for the art itself to land around 130 dp.
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(220.dp),
        )
        Text(
            text = stringResource(R.string.todo_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
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
            shape = MaterialTheme.shapes.medium,
            colors = clipboardFieldColors(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.weight(1f),
        )
        FilledIconButton(
            onClick = submit,
            enabled = text.isNotBlank(),
            shape = MaterialTheme.shapes.small,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            modifier = Modifier
                .padding(start = 10.dp)
                .size(52.dp)
                // Orange on khaki is only 1.7:1, so the ink outline defines the button edge.
                .border(1.5.dp, MaterialTheme.shiba.cardOutline, MaterialTheme.shapes.small),
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.todo_add))
        }
    }
}

/** Text field colours that read like a line on the clipboard. */
@Composable
private fun clipboardFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.shiba.card,
    unfocusedContainerColor = MaterialTheme.shiba.card,
    errorContainerColor = MaterialTheme.shiba.card,
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = MaterialTheme.shiba.cardOutline,
    cursorColor = MaterialTheme.colorScheme.secondary,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

@Composable
private fun TodoRow(
    todo: Todo,
    edit: EditState?,
    revealIndex: Int,
    revealed: Boolean,
    onSetDone: (Todo, Boolean) -> Unit,
    onDelete: (Todo) -> Unit,
    onStartEdit: (Todo) -> Unit,
    onSaveEdit: (Todo, String) -> Unit,
    onCancelEdit: () -> Unit,
    onEditTextChanged: () -> Unit,
) {
    ClipboardCard(modifier = Modifier.padding(vertical = 5.dp)) {
        if (edit != null) {
            EditTodoRow(
                todo = todo,
                showEmptyError = edit.showEmptyError,
                onSave = { onSaveEdit(todo, it) },
                onCancel = onCancelEdit,
                onTextChanged = onEditTextChanged,
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ShibaCheckbox(
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
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                )
                IconButton(onClick = { onStartEdit(todo) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.todo_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { onDelete(todo) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.todo_delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Inline editor that replaces a row. Draft text lives here; validation lives in the ViewModel. */
@Composable
private fun EditTodoRow(
    todo: Todo,
    showEmptyError: Boolean,
    onSave: (String) -> Unit,
    onCancel: () -> Unit,
    onTextChanged: () -> Unit,
) {
    var draft by remember(todo.id) {
        mutableStateOf(TextFieldValue(todo.title, selection = TextRange(todo.title.length)))
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(todo.id) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = {
                draft = it
                onTextChanged()
            },
            singleLine = true,
            isError = showEmptyError,
            shape = MaterialTheme.shapes.small,
            colors = clipboardFieldColors(),
            supportingText = if (showEmptyError) {
                { Text(stringResource(R.string.todo_edit_empty_error)) }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSave(draft.text) }),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
        )
        IconButton(onClick = { onSave(draft.text) }) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.todo_save),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        IconButton(onClick = onCancel) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.todo_cancel),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
