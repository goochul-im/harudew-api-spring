package b1a4.harudew.recommend.port.out

import b1a4.harudew.recommend.domain.RoutineType
import com.fasterxml.jackson.annotation.JsonProperty

data class RecommendRoutineResponse(
    @field:JsonProperty("routineId")
    val id: Long,
    @field:JsonProperty("routineType")
    val type: RoutineType,
    val content: String
)
