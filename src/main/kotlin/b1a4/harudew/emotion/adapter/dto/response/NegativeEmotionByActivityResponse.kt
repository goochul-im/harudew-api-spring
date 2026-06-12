package b1a4.harudew.emotion.adapter.dto.response

data class NegativeEmotionByActivityResponse(
    val stress: List<ActivityEmotionSummaryResponse>,
    val depression: List<ActivityEmotionSummaryResponse>,
    val anxiety: List<ActivityEmotionSummaryResponse>,
)
