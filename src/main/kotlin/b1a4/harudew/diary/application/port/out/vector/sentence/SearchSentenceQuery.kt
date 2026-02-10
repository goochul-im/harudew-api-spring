package b1a4.harudew.diary.application.port.out.vector.sentence

data class SearchSentenceQuery(
    val vector: List<Number>,
    val authorId: String,
    val limit: Int = 5
)
