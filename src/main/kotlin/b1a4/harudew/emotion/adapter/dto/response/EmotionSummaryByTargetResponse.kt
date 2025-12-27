package b1a4.harudew.emotion.adapter.dto.response

import b1a4.harudew.emotion.domain.EmotionType
import java.time.LocalDate

data class EmotionSummaryByTargetResponse(
    val date: LocalDate,
    val emotions: List<EmotionDetailResponse>
)

data class EmotionDetailResponse(
    val emotion: EmotionType,
    val count: Int,
    val intensity: Number
)
