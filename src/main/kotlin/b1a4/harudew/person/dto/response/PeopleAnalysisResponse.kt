package b1a4.harudew.person.dto.response

import b1a4.harudew.diary.adapter.dto.response.EmotionAnalysisResponse

data class PeopleAnalysisResponse(
    val name: String,
    val feel: List<EmotionAnalysisResponse>,
    val count: Int
)
