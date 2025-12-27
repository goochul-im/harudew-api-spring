package b1a4.harudew.emotion.adapter.dto.response

import b1a4.harudew.emotion.domain.EmotionGroup
import java.time.LocalDate

data class EmotionAnalysisPeriodResponse(
    val activities: ActivityEmotionSummaryResponse,
    val people: TargetEmotionSummaryResponse,
    val date: EmotionSummaryPeriodResponse
)

data class EmotionSummaryPeriodResponse(
    val date: LocalDate,
    val emotionGroup: EmotionGroup,
    val intensity: Long,
    val count: Int
)

data class TargetEmotionSummaryResponse(
    val targetId: Long,
    val targetName: String,
    val emotion: EmotionGroup,
    val totalIntensity: Long,
    val percentage: Int
)

data class ActivityEmotionSummaryResponse(
    val activityId: Long,
    val activityContent: String,
    val emotion: EmotionGroup,
    val totalIntensity: Long,
    val count: Int,
    val percentage: Int
)
