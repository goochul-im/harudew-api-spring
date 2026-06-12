package b1a4.harudew.todo.domain

import java.time.LocalDate

/**
 * 일기 분석에서 추출된 할 일 원본.
 */
data class DiaryTodo(
    val id: Long? = null,
    val diaryId: Long,
    val authorId: String,
    val content: String,
    val createdAt: LocalDate
)
