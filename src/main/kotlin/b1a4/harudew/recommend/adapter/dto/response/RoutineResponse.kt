package b1a4.harudew.recommend.adapter.dto.response

import b1a4.harudew.recommend.domain.RoutineType
import com.fasterxml.jackson.annotation.JsonProperty

data class RoutineResponse(
    @JsonProperty("routineId")
    val id: Long,
    val content: String,
    val routineType: RoutineType,
    val isTrigger: Boolean
)
