package b1a4.harudew.emotion.adapter.dto.response

import b1a4.harudew.diary.adapter.dto.response.DiaryResponse
import com.fasterxml.jackson.annotation.JsonProperty

data class TargetDetailAnalysisResponse(
    @JsonProperty("targetId")
    val id: Long,
    @JsonProperty("targetName")
    val name: String,
    val closenessScore: Int,
    val emotions: List<EmotionDetailResponse>,
    val diaries: List<DiaryResponse>,
    val activities: List<TargetActivityResponse>
)

data class TargetActivityResponse(
    val content: String,
    val count: Int
)
