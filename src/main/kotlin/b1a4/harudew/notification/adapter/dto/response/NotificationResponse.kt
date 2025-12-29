package b1a4.harudew.notification.adapter.dto.response

import b1a4.harudew.notification.domain.NotificationType
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class NotificationResponse(
    val id: Long,
    val content: String,
    @JsonProperty("isRead")
    val isRead: Boolean,
    val createdAt: LocalDate,
    val type: NotificationType,
    val photoPath: List<String>,
    val diaryId: Long,
    @JsonProperty("targetDate")
    val relationDate: LocalDate
)
