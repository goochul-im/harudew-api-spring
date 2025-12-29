package b1a4.harudew.recommend.adapter.dto.response

import b1a4.harudew.emotion.domain.EmotionType

data class RecommendVideoResponse(
    val videoId: Long,
    val emotion: EmotionType,
    val message: String
)
