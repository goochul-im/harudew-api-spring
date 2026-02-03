package b1a4.harudew.diary.application.port.out.vector.sentence

import b1a4.harudew.diary.application.port.out.vector.ContentVectorWrapper
import java.time.LocalDate

data class SaveSentenceRequest(
    val sentences: List<ContentVectorWrapper>,
    val diaryId: Long,
    val authorId: String,
    val date: LocalDate
)
