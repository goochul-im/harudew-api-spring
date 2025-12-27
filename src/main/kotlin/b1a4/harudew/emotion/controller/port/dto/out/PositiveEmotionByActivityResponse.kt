package b1a4.harudew.emotion.controller.port.dto.out

data class PositiveEmotionByActivityResponse(
    val stability : ActivityEmotionSummaryResponse,
    val bond : ActivityEmotionSummaryResponse,
    val vitality : ActivityEmotionSummaryResponse,
)
