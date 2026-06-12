package b1a4.harudew.member.adapter.dto.response

import b1a4.harudew.emotion.domain.EmotionType
import com.fasterxml.jackson.annotation.JsonProperty

data class EmotionBaseAnalysisRes(
    @JsonProperty("Relation")
    val relation: List<EmotionBaseAnalysis>,
    @JsonProperty("Self")
    val self: List<EmotionBaseAnalysis>,
    @JsonProperty("State")
    val state: List<EmotionBaseAnalysis>
)

data class EmotionBaseAnalysis(
    val emotion: String,
    val intensity: Number,
    val count: Number
)
