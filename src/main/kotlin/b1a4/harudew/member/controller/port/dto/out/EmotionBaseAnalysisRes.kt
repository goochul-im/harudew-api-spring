package b1a4.harudew.member.controller.port.dto.out

import b1a4.harudew.emotion.domain.EmotionType
import com.fasterxml.jackson.annotation.JsonProperty

data class EmotionBaseAnalysisRes(
    @JsonProperty("Relation")
    val relation: EmotionBaseAnalysis,
    @JsonProperty("Self")
    val self: EmotionBaseAnalysis,
    @JsonProperty("State")
    val state: EmotionBaseAnalysis
)

data class EmotionBaseAnalysis(
    val emotion: EmotionType,
    val intensity: Number,
    val count: Number
)
