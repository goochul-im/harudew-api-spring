package b1a4.harudew.notification.domain

import b1a4.harudew.diary.domain.Diary
import b1a4.harudew.member.domain.Member
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 알림 도메인
 * @param id
 * @param content 본문
 * @param type 알림 타입
 * @param photoPath 알림에 포함된 사진 리스트
 * @param createDate 생성 날짜와 시간
 * @param isRead 읽음 여부
 * @param diaryId 포함된 일기 아이디
 * @param targetDate 알림이 특정 날짜와 연관되어 있을 때, 그 날짜를 저장 (Todo 마감일 등)
 * @param author 알림의 주인
 */
class Notification(
    val id: Long,
    val content: String,
    val type: NotificationType,
    val photoPath: Array<String>,
    val createDate: LocalDateTime,
    val isRead: Boolean,
    val diaryId: Long?,
    val targetDate: LocalDate?,
    val author: Member
) {
}

enum class NotificationType {
    RECAP,
    TODO,
    CHARACTER,
    ROUTINE,
    TODAY_COMMENT,
}
