package b1a4.harudew.diary.application.port.out.vector.keyword

import b1a4.harudew.diary.application.port.out.vector.ContentVectorWrapper

data class SaveKeywordRequest(
    val keywords: List<ContentVectorWrapper>,
    val diaryId: Long,
    val authorId: String
)
