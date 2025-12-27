package b1a4.harudew.emotion.controller.dto.out

data class NegativeEmotionByActivityResponse(
    val stress : ActivityEmotionSummaryResponse,
    val depression : ActivityEmotionSummaryResponse,
    val anxiety : ActivityEmotionSummaryResponse,
) {
}
