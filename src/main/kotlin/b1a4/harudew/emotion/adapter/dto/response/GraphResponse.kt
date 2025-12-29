package b1a4.harudew.emotion.adapter.dto.response

import b1a4.harudew.emotion.domain.EmotionType

data class GraphResponse(
    val todayEmotions: GraphEmotionResponse,
    val relations: WillBeDeprecatedDTO
)

data class GraphEmotionResponse(
    val emotion: EmotionType,
    val intensity: Int
)

data class WillBeDeprecatedDTO(
    val relations: List<TargetEmotionResponse>
)

data class TargetEmotionResponse(
    val id: Long,
    val name: String,
    val affection: Number,
    val emotions: EmotionDetailResponse,
    val count: Int
)
