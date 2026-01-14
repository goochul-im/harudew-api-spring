package b1a4.harudew.diary.application.port.out.dto

data class SaveSentenceRequest(
    val sentences: List<ContentVectorWrapper>,
    val diaryId: Long,
    val authorId: Long
)
