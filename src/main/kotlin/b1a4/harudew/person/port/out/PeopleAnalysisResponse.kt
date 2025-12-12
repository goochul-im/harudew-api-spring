package b1a4.harudew.person.port.out

import b1a4.harudew.emotion.port.out.EmotionAnalysisResponse

data class PeopleAnalysisResponse(
    val name: String,
    val feel: List<EmotionAnalysisResponse>,
    val count: Int
)
