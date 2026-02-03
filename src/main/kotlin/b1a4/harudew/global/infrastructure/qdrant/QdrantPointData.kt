package b1a4.harudew.global.infrastructure.qdrant

import java.util.UUID

data class QdrantPointData(
    val id: UUID? = null,
    val vector: List<Number>,
    val payload: Map<String, Any>
)
