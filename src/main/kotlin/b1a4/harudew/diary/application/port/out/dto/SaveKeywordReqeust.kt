package b1a4.harudew.diary.application.port.out.dto

data class SaveKeywordReqeust(
    val keywords: List<ContentVectorWrapper>,
    val diaryId: Long,
    val authorId: Long
)
