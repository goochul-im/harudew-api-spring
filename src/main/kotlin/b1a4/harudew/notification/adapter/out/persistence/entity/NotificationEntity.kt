package b1a4.harudew.notification.adapter.out.persistence.entity

import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import b1a4.harudew.notification.adapter.dto.response.NotificationResponse
import b1a4.harudew.notification.domain.NotificationType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "notification")
class NotificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: NotificationType,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_read", nullable = false)
    var read: Boolean = false,

    @Column(name = "target_date")
    val targetDate: LocalDate? = null,

    @Column(name = "diary_id")
    val diaryId: Long? = null,

    @Column(name = "photo_path")
    val photoPath: String? = null
) {
    fun toResponse() = NotificationResponse(
        id = requireNotNull(id),
        content = content,
        createdAt = createdAt,
        read = read,
        type = type,
        targetDate = targetDate,
        diaryId = diaryId,
        photoPath = photoPath
    )
}
