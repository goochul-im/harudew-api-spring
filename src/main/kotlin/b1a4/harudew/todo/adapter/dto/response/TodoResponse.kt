package b1a4.harudew.todo.adapter.dto.response

import com.fasterxml.jackson.annotation.JsonFormat

data class TodoResponse(
    val id: Long,
    val title: String,
    val content: String = title,
    val isComplete: Boolean = false,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val date: java.time.LocalDate? = null
)
