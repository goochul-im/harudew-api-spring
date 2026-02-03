package b1a4.harudew.diary.adapter.dto.response

import b1a4.harudew.activity.adapter.dto.response.ActivitySimpleResponse
import b1a4.harudew.person.dto.response.PeopleAnalysisResponse
import b1a4.harudew.todo.adapter.dto.response.TodoAnalysisResponse
import com.fasterxml.jackson.annotation.JsonProperty

data class DiaryAnalysisResult(
    val id: Long,
    val title: String,
    val photoPath: List<String>?,
    val content: String,
    val people: List<PeopleAnalysisResponse>,
    @field:JsonProperty("selfEmotion")
    val selfEmotions: List<EmotionAnalysisResponse>,
    @field:JsonProperty("stateEmotion")
    val stateEmotions: List<EmotionAnalysisResponse>,
    @field:JsonProperty("activity")
    val activities: List<ActivitySimpleResponse>,
    val todos: List<TodoAnalysisResponse>
)
