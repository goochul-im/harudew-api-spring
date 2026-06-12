package b1a4.harudew.todo.application.port.out

import b1a4.harudew.todo.domain.DiaryTodo

interface DiaryTodoRepository {

    fun saveAll(diaryTodos: List<DiaryTodo>): List<DiaryTodo>
}
