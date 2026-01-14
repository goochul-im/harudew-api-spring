package b1a4.harudew.diary.application.port.out.rerank

data class RerankResult(
    val id: Long,
    val text: String,
    val score: Double
)
