package b1a4.harudew.emotion.controller.dto.out

data class NegativeEmotionByTargetResponse(
    val stressTarget: TargetEmotionSummaryResponse,
    val depressionTarget: TargetEmotionSummaryResponse,
    val anxietyTarget: TargetEmotionSummaryResponse,
    val stressDate: EmotionSummaryPeriodResponse,
    val depressionDate: EmotionSummaryPeriodResponse,
    val anxietyDate: EmotionSummaryPeriodResponse
)
