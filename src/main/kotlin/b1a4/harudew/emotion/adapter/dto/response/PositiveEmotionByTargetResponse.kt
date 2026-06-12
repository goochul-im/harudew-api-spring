package b1a4.harudew.emotion.adapter.dto.response

data class PositiveEmotionByTargetResponse(
    val stabilityTarget: List<TargetEmotionSummaryResponse>,
    val bondTarget: List<TargetEmotionSummaryResponse>,
    val vitalityTarget: List<TargetEmotionSummaryResponse>,
    val vialityTarget: List<TargetEmotionSummaryResponse> = vitalityTarget,
    val stabilityDate: List<EmotionSummaryPeriodResponse>,
    val bondDate: List<EmotionSummaryPeriodResponse>,
    val vitalityDate: List<EmotionSummaryPeriodResponse>,
    val vialityDate: List<EmotionSummaryPeriodResponse> = vitalityDate
)
