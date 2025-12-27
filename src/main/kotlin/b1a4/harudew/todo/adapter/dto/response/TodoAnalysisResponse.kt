package b1a4.harudew.todo.adapter.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class TodoAnalysisResponse(
    @field:JsonProperty("Todocontent")
    val todoContent: String
)
