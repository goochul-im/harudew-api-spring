package b1a4.harudew.todo.domain

import b1a4.harudew.diary.domain.model.Diary
import b1a4.harudew.member.domain.Member
import java.time.LocalDateTime

/**
 * 사용자의 일기 내용을 분석하여 자동으로 추출/제안된 할 일
 * @param id
 * @param content 일기에서 추출된 투두 텍스트
 * @param diary 분석된 원본 일기
 * @param member 분석된 일기의 주인
 * @param createdAt 생성 날짜
 */
class DiaryTodo(
    val id: Long,
    val content: String,
    val diary: Diary,
    val member: Member,
    val createdAt: LocalDateTime
) {
}
