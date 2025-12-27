package b1a4.harudew.emotion.controller.port.dto.out

data class PositiveEmotionByTargetResponse(
    val stabilityTarget: TargetEmotionSummaryResponse,
    val bondTarget: TargetEmotionSummaryResponse,
    val vitalityTarget: TargetEmotionSummaryResponse,
    val stabilityDate: EmotionSummaryPeriodResponse,
    val bondDate: EmotionSummaryPeriodResponse,
    val vitalityDate: EmotionSummaryPeriodResponse
)
