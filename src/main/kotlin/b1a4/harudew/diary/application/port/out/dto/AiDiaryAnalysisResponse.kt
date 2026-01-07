package b1a4.harudew.diary.application.port.out.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class DiaryAnalysisResponse(
    @JsonProperty("activity_analysis")
    val activityAnalysis: List<ActivityAnalysis>,
    val reflection: Reflection
)

data class ActivityAnalysis(
    val activity: String,
    val peoples: List<PersonAnalysis>,
    @JsonProperty("self_emotions")
    val selfEmotions: List<EmotionData>,
    @JsonProperty("state_emotions")
    val stateEmotions: List<EmotionData>,
    val problem: List<ProblemAnalysis>,
    val strength: String
)

data class PersonAnalysis(
    val name: String,
    val interactions: List<EmotionData>,
    @JsonProperty("name_intimacy")
    val nameIntimacy: String
)

data class EmotionData(
    val emotion: String,
    @JsonProperty("emotion_intensity")
    val emotionIntensity: Int
)

data class ProblemAnalysis(
    val situation: String,
    val approach: String,
    val outcome: String,
    @JsonProperty("conflict_response_code")
    val conflictResponseCode: String
)

data class Reflection(
    val achievements: List<String>,
    val shortcomings: List<String>,
    val todo: List<String>
)
