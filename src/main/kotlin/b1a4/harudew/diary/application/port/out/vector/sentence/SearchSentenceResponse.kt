package b1a4.harudew.diary.application.port.out.vector.sentence

import java.time.LocalDate

data class SearchSentenceResponse(
    val sentence: String,
    val diaryId: Long,
    val date: LocalDate,
    val score: Float
)
