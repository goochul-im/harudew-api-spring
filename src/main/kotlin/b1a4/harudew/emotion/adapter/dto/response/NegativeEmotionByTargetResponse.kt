package b1a4.harudew.emotion.adapter.dto.response

data class NegativeEmotionByTargetResponse(
    val stressTarget: List<TargetEmotionSummaryResponse>,
    val depressionTarget: List<TargetEmotionSummaryResponse>,
    val anxietyTarget: List<TargetEmotionSummaryResponse>,
    val stressDate: List<EmotionSummaryPeriodResponse>,
    val depressionDate: List<EmotionSummaryPeriodResponse>,
    val anxietyDate: List<EmotionSummaryPeriodResponse>
)
