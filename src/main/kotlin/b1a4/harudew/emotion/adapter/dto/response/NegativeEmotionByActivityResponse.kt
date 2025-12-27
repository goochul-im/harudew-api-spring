package b1a4.harudew.emotion.adapter.dto.response

data class NegativeEmotionByActivityResponse(
    val stress : ActivityEmotionSummaryResponse,
    val depression : ActivityEmotionSummaryResponse,
    val anxiety : ActivityEmotionSummaryResponse,
) {
}
