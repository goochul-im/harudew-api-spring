package b1a4.harudew.emotion.port.out

import java.time.LocalDate

data class DiaryEmotionScoreResponse(
    val scores: List<EmotionScoreResponse>
)

data class EmotionScoreResponse(
    val id: Long,
    val writtenDate: LocalDate,
    val intensitySum: Number
)
