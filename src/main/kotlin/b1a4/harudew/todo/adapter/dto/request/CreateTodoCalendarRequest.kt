package b1a4.harudew.todo.adapter.dto.request

import java.time.LocalDate

data class CreateTodoCalendarRequest(
    val content: String,
    val date: LocalDate
)
