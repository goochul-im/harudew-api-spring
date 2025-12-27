package b1a4.harudew.todo.controller.dto.out

data class TodoCalendarResponse(
    val id: Long,
    val isCompleted: Boolean,
    val content: String
)
