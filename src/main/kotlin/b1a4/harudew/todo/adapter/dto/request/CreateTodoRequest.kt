package b1a4.harudew.todo.adapter.dto.request

data class CreateTodoRequest(
    val title: String,
    val content: String? = null,
    val date: java.time.LocalDate? = null,
)
