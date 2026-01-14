package b1a4.harudew.diary.application.port.out.vector

/**
 * @param content 벡터화 되기 전 내용
 * @param vector 벡터
 */
data class ContentVectorWrapper(
    val content: String,
    val vector: List<Double>
)
