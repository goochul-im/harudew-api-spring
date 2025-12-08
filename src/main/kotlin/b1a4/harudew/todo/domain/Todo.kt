package b1a4.harudew.todo.domain

import b1a4.harudew.member.domain.Member
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 할일 도메인
 * @param id
 * @param title 제목
 * @param isCompleted 완료 여부
 * @param date 이 투두가 생성된 곳
 * @param isRepeat 반복 여부
 * @param repeatRule 반복 룰
 * @param repeatEndDate 반복 완료 시간
 * @param createdAt 생성 시간
 * @param updatedAt 업데이트 시간
 * @param owner 생성자, 주인
 */
class Todo(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val date: LocalDate? = null,
    val isRepeat: Boolean = false,
    val repeatRule: String? = null,
    val repeatEndDate: LocalDate? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val owner: Member
)
