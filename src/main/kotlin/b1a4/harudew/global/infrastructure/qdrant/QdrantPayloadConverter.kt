package b1a4.harudew.global.infrastructure.qdrant

import io.qdrant.client.ValueFactory
import io.qdrant.client.grpc.JsonWithInt.Value
import java.time.LocalDate
import java.time.LocalDateTime

object QdrantPayloadConverter {

    fun toQdrantPayload(map: Map<String, Any>): Map<String, Value> {
        return map.mapValues { (_, value) -> toValue(value) }
    }

    private fun toValue(value: Any): Value {
        return when (value) {
            is String -> ValueFactory.value(value)
            is Long -> ValueFactory.value(value)
            is Int -> ValueFactory.value(value.toLong())
            is Double -> ValueFactory.value(value)
            is Float -> ValueFactory.value(value.toDouble())
            is Boolean -> ValueFactory.value(value)
            is LocalDate -> ValueFactory.value(value.toString())
            is LocalDateTime -> ValueFactory.value(value.toString())
            is List<*> -> ValueFactory.list(value.filterNotNull().map { toValue(it) })
            else -> ValueFactory.value(value.toString())
        }
    }

    fun fromQdrantPayload(payload: Map<String, Value>): Map<String, Any?> {
        return payload.mapValues { (_, value) -> fromValue(value) }
    }

    private fun fromValue(value: Value): Any? {
        return when {
            value.hasStringValue() -> value.stringValue
            value.hasIntegerValue() -> value.integerValue
            value.hasDoubleValue() -> value.doubleValue
            value.hasBoolValue() -> value.boolValue
            value.hasListValue() -> value.listValue.valuesList.map { fromValue(it) }
            value.hasNullValue() -> null
            else -> null
        }
    }
}
