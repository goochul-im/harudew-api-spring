package b1a4.harudew.todo.controller.dto.out

import java.time.LocalDate

data class TodoCalendarByMonthResponse(
    val date: LocalDate,
    val todoTotalCount: Int,
    val completedCount: Int,
    val isAllCompleted: Boolean
)
