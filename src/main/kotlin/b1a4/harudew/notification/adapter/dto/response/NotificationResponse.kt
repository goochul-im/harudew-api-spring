package b1a4.harudew.notification.adapter.dto.response

import b1a4.harudew.notification.domain.NotificationType
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val content: String,
    @field:JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val createdAt: LocalDateTime,
    val read: Boolean,
    val type: NotificationType,
    val targetDate: LocalDate? = null,
    val diaryId: Long? = null,
    val photoPath: String? = null
)
