package b1a4.harudew.emotion.adapter.dto.response

data class PositiveEmotionByActivityResponse(
    val stability : ActivityEmotionSummaryResponse,
    val bond : ActivityEmotionSummaryResponse,
    val vitality : ActivityEmotionSummaryResponse,
)
