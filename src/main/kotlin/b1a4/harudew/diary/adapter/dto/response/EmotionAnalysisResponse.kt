package b1a4.harudew.diary.adapter.dto.response

import b1a4.harudew.emotion.domain.EmotionType

data class EmotionAnalysisResponse(
    val emotionType: EmotionType,
    val intensity: Number
)
