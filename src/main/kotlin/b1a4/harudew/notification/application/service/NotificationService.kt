package b1a4.harudew.notification.application.service

import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.notification.adapter.dto.response.NotificationResponse
import b1a4.harudew.notification.adapter.dto.response.UnreadNotificationCountResponse
import b1a4.harudew.notification.adapter.out.persistence.NotificationJpaRepository
import b1a4.harudew.notification.adapter.out.persistence.entity.NotificationEntity
import b1a4.harudew.notification.domain.NotificationType
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class NotificationService(
    private val notificationJpaRepository: NotificationJpaRepository,
    private val memberJpaRepository: MemberJpaRepository
) {

    fun findUnread(memberId: String): List<NotificationResponse> =
        notificationJpaRepository.findUnreadByMember(memberId).map { it.toResponse() }

    fun findAll(memberId: String): List<NotificationResponse> =
        notificationJpaRepository.findAllByMember(memberId).ifEmpty {
            listOf(createWelcomeNotification(memberId))
        }.map { it.toResponse() }

    fun countUnread(memberId: String): UnreadNotificationCountResponse =
        UnreadNotificationCountResponse(notificationJpaRepository.countUnreadByMember(memberId))

    fun read(memberId: String, id: Long) {
        val notification = notificationJpaRepository.findOwnedById(id, memberId)
            .orElseThrow { NoSuchElementException("알림을 찾을 수 없습니다. id=$id") }
        notification.read = true
        notificationJpaRepository.save(notification)
    }

    fun createTest(memberId: String): NotificationResponse =
        notificationJpaRepository.save(
            NotificationEntity(
                member = memberJpaRepository.getReferenceById(memberId),
                content = "테스트 알림입니다.",
                type = NotificationType.RECAP,
                diaryId = null,
                targetDate = LocalDate.now()
            )
        ).toResponse()

    private fun createWelcomeNotification(memberId: String): NotificationEntity =
        notificationJpaRepository.save(
            NotificationEntity(
                member = memberJpaRepository.getReferenceById(memberId),
                content = "하루 기록을 시작해보세요.",
                type = NotificationType.TODAY_COMMENT
            )
        )
}
