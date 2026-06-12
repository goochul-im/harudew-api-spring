package b1a4.harudew.todo.adapter.dto.response

import java.time.LocalDate
import com.fasterxml.jackson.annotation.JsonFormat

data class TodoCalendarByMonthResponse(
    @field:JsonFormat(pattern = "yyyy-MM-dd")
    val date: LocalDate,
    val todoTotalCount: Int,
    val completedCount: Int,
    val isAllCompleted: Boolean
)
