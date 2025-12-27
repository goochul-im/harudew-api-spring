package b1a4.harudew.todo.controller.dto.`in`

import java.time.LocalDate

data class CreateTodoCalendarRequest(
    val content: String,
    val date: LocalDate
)
