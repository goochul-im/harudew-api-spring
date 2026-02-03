package b1a4.harudew.global.infrastructure.qdrant

import b1a4.harudew.global.util.UuidGenerator
import io.qdrant.client.ConditionFactory.match
import io.qdrant.client.PointIdFactory.id
import io.qdrant.client.QdrantClient
import io.qdrant.client.VectorsFactory.vectors
import io.qdrant.client.WithPayloadSelectorFactory.enable
import io.qdrant.client.grpc.Common
import io.qdrant.client.grpc.Points
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class QdrantClientAdapter(
    private val qdrantClient: QdrantClient,
    private val uuidGenerator: UuidGenerator
) : QdrantClientPort {

    override fun upsertAsync(collectionName: String, points: List<QdrantPointData>) {
        if (points.isEmpty()) {
            return
        }

        val qdrantPoints = points.map { point ->
            Points.PointStruct.newBuilder()
                .setId(id(point.id ?: uuidGenerator.generate()))
                .setVectors(vectors(point.vector.map { it.toFloat() }))
                .putAllPayload(QdrantPayloadConverter.toQdrantPayload(point.payload))
                .build()
        }

        qdrantClient.upsertAsync(collectionName, qdrantPoints)
    }

    override fun search(query: QdrantSearchQuery): List<QdrantSearchResult> {
        val searchRequestBuilder = Points.SearchPoints.newBuilder()
            .setCollectionName(query.collectionName)
            .addAllVector(query.vector.map { it.toFloat() })
            .setWithPayload(enable(query.withPayload))
            .setLimit(query.limit.toLong())

        if (query.filters.isNotEmpty()) {
            val filterBuilder = Common.Filter.newBuilder()
            query.filters.forEach { (key, value) ->
                when (value) {
                    is Long -> filterBuilder.addMust(match(key, value))
                    is Int -> filterBuilder.addMust(match(key, value.toLong()))
                }
            }
            searchRequestBuilder.setFilter(filterBuilder.build())
        }

        query.scoreThreshold?.let {
            searchRequestBuilder.setScoreThreshold(it)
        }

        val scoredPoints = qdrantClient.searchAsync(searchRequestBuilder.build()).get()

        return scoredPoints.map { point ->
            QdrantSearchResult(
                id = extractUuid(point.id),
                score = point.score,
                payload = QdrantPayloadConverter.fromQdrantPayload(point.payloadMap)
            )
        }
    }

    override fun deleteByFilter(collectionName: String, filter: Map<String, Any>) {
        val filterBuilder = Common.Filter.newBuilder()
        filter.forEach { (key, value) ->
            when (value) {
                is Long -> filterBuilder.addMust(match(key, value))
                is Int -> filterBuilder.addMust(match(key, value.toLong()))
            }
        }

        qdrantClient.deleteAsync(collectionName, filterBuilder.build())
    }

    private fun extractUuid(pointId: Common.PointId): UUID {
        return when {
            pointId.uuid.isNotEmpty() -> UUID.fromString(pointId.uuid)
            pointId.num > 0 -> UUID(0, pointId.num)
            else -> uuidGenerator.generate()
        }
    }
}
