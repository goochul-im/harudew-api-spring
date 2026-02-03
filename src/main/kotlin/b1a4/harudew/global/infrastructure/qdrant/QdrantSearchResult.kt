package b1a4.harudew.global.infrastructure.qdrant

import java.util.UUID

data class QdrantSearchResult(
    val id: UUID,
    val score: Float,
    val payload: Map<String, Any?>
) {
    fun getString(key: String): String? = payload[key] as? String

    fun getLong(key: String): Long? = when (val value = payload[key]) {
        is Long -> value
        is Int -> value.toLong()
        is Number -> value.toLong()
        else -> null
    }

    fun getDouble(key: String): Double? = when (val value = payload[key]) {
        is Double -> value
        is Float -> value.toDouble()
        is Number -> value.toDouble()
        else -> null
    }
}
