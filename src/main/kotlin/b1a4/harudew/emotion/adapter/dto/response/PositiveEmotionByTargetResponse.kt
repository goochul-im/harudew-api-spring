package b1a4.harudew.emotion.adapter.dto.response

data class PositiveEmotionByTargetResponse(
    val stabilityTarget: TargetEmotionSummaryResponse,
    val bondTarget: TargetEmotionSummaryResponse,
    val vitalityTarget: TargetEmotionSummaryResponse,
    val stabilityDate: EmotionSummaryPeriodResponse,
    val bondDate: EmotionSummaryPeriodResponse,
    val vitalityDate: EmotionSummaryPeriodResponse
)
