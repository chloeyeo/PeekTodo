package com.chloeyeo.peektodo.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(
    private val dao: TodoDao,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    fun observeAll(): Flow<List<Todo>> = dao.observeAll()

    fun observeOpen(): Flow<List<Todo>> = dao.observeOpen()

    suspend fun getOpen(): List<Todo> = dao.getOpen()

    suspend fun add(title: String): Long {
        val now = clock()
        return dao.insert(Todo(title = title, createdAt = now, updatedAt = now))
    }

    suspend fun setDone(id: Long, done: Boolean) = dao.setDone(id, done, clock())

    suspend fun rename(todo: Todo, title: String) =
        dao.update(todo.copy(title = title, updatedAt = clock()))

    suspend fun delete(todo: Todo) = dao.delete(todo)
}
