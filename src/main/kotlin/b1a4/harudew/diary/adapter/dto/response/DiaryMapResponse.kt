package b1a4.harudew.diary.adapter.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class DiaryMapResponse(
    val result: DiaryMapInfo
)

data class DiaryMapInfo(
    val latitude: Double,
    val longitude: Double,
    val diaryId: Long,
    @JsonProperty("photo_path")
    val photoPath: List<String>,
    val content: String
)
