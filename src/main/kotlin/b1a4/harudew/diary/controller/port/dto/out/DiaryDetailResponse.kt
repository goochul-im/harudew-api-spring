package b1a4.harudew.diary.controller.port.dto.out

import b1a4.harudew.emotion.port.out.EmotionAnalysisResponse
import b1a4.harudew.emotion.port.out.DiaryEmotionScoreResponse
import b1a4.harudew.person.port.out.PeopleAnalysisResponse
import b1a4.harudew.recommend.port.out.RecommendRoutineResponse
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class DiaryDetailResponse(
    val id: Long,
    val writtenDate: LocalDate,
    val photoPath: List<String>?,
    val audioPath: List<String>?,
    val isBookmarked: Boolean,
    val people: List<PeopleAnalysisResponse>,
    val content: String,
    val emotions: List<EmotionAnalysisResponse>,
    val latitude: Float,
    val longitude: Float,
    val stressWarning: Boolean,
    val anxietyWarning: Boolean,
    val depressionWarning: Boolean,
    val recommendRoutine: RecommendRoutineResponse,
    val beforeDiaryScores: DiaryEmotionScoreResponse,
    val analysis: JsonResponse
)

/**
 * 이하 아래의 데이터 클래스들은 Json을 그대로 옮기기 위한 클래스이며,
 * DiaryJsonResponse 이외에는 사용되지 않습니다.
 * 추후 리팩토링이 강력히 권장됩니다.
 */


data class JsonResponse(
    @field:JsonProperty("activity_analysis")
    val activityAnalysis: List<JsonActivityResponse>,
    val reflection: JsonReflectionResponse
)

data class JsonReflectionResponse(
    val achievement: List<String>,
    val shortcomings: List<String>,
    @field:JsonProperty("tomorrow_mindset")
    val tomorrowMindset: String,
    @field:JsonProperty("todo")
    val todos: List<String>
)

data class JsonActivityResponse(
    @field:JsonProperty("activity")
    val activityName: String,
    val people: List<JsonPeopleResponse>,
    @field:JsonProperty("self_emotions")
    val selfEmotions: JsonEmotionResponse,
    @field:JsonProperty("state_emotions")
    val stateEmotions: JsonEmotionResponse,
    val problem : List<JsonProblemAnalysis>,
    val strength: String
)

data class JsonPeopleResponse(
    val name: String,
    val interactions: JsonEmotionResponse,
    val nameIntensity: Int
)

data class JsonEmotionResponse(
    val emotion: List<String>,
    val intensity: List<Int>
)

data class JsonProblemAnalysis(
    val situation: String,
    val approach: String,
    val outcome: String,
    @field:JsonProperty("conflict_response_code")
    val conflictResponseCode: String
)
