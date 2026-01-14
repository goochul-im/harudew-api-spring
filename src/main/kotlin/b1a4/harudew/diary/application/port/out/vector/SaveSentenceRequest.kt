package b1a4.harudew.diary.application.port.out.vector

data class SaveSentenceRequest(
    val sentences: List<ContentVectorWrapper>,
    val diaryId: Long,
    val authorId: Long
)
