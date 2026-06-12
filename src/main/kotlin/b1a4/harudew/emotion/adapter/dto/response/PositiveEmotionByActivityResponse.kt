package b1a4.harudew.emotion.adapter.dto.response

data class PositiveEmotionByActivityResponse(
    val stability: List<ActivityEmotionSummaryResponse>,
    val bond: List<ActivityEmotionSummaryResponse>,
    val vitality: List<ActivityEmotionSummaryResponse>,
    val viality: List<ActivityEmotionSummaryResponse> = vitality,
)
