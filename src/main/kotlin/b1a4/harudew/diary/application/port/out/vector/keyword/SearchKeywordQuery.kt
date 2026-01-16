package b1a4.harudew.diary.application.port.out.vector.keyword

data class SearchKeywordQuery(
    val keyword: String,
    val diaryId: Long,
    val vector: List<Number>,
    val authorId: Long
)
