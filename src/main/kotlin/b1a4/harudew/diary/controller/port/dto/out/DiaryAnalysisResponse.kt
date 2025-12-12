package b1a4.harudew.diary.controller.port.dto.out

import b1a4.harudew.activity.port.out.ActivitySimpleResponse
import b1a4.harudew.emotion.port.out.EmotionAnalysisResponse
import b1a4.harudew.person.port.out.PeopleAnalysisResponse
import b1a4.harudew.todo.port.out.TodoAnalysisResponse
import com.fasterxml.jackson.annotation.JsonProperty

data class DiaryAnalysisResponse(
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
