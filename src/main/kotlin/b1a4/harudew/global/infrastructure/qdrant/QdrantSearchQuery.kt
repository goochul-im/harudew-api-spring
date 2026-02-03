package b1a4.harudew.global.infrastructure.qdrant

data class QdrantSearchQuery(
    val collectionName: String,
    val vector: List<Number>,
    val filters: Map<String, Any> = emptyMap(),
    val limit: Int = 10,
    val withPayload: Boolean = true,
    val scoreThreshold: Float? = null
)
