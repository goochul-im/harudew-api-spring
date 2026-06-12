package b1a4.harudew.todo.adapter.dto.response

import com.fasterxml.jackson.annotation.JsonFormat

data class TodoCalendarResponse(
    val id: Long,
    val content: String,
    val isComplete: Boolean,
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val date: java.time.LocalDate
)
