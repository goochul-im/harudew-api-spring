package b1a4.harudew.todo.domain

import b1a4.harudew.member.domain.Member
import java.time.LocalDate

/**
 * 특정 날짜에 수행해야 하는 구체적인 할 일 인스턴스 도메인
 * @param id
 * @param content 투두 내용
 * @param isCompleted 완료 여부
 * @param date 배정된 날짜
 * @param author 주인
 */
class TodoCalendar(
    val id: Long,
    val content: String,
    val isCompleted: Boolean,
    val date: LocalDate,
    val author: Member
) {
}
