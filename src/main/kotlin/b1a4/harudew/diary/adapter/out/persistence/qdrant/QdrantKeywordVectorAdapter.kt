package b1a4.harudew.diary.adapter.out.persistence.qdrant

import b1a4.harudew.diary.application.port.out.vector.keyword.KeywordVectorPort
import b1a4.harudew.diary.application.port.out.vector.keyword.SaveKeywordReqeust
import b1a4.harudew.diary.application.port.out.vector.keyword.SearchKeywordQuery
import b1a4.harudew.diary.application.port.out.vector.keyword.SearchKeywordResult
import b1a4.harudew.global.util.UuidGenerator
import io.qdrant.client.PointIdFactory.id
import io.qdrant.client.QdrantClient
import io.qdrant.client.VectorsFactory.vectors
import io.qdrant.client.grpc.Points
import org.springframework.stereotype.Component
import io.qdrant.client.ValueFactory.value
import io.qdrant.client.VectorFactory.vector
import io.qdrant.client.WithPayloadSelectorFactory.enable
import io.qdrant.client.ConditionFactory.match
import io.qdrant.client.grpc.Common

@Component
class QdrantKeywordVectorAdapter(
    private val qdrantClient: QdrantClient,
    private val uuidGenerator: UuidGenerator,
    private val collectionName: String = "keyword"
) : KeywordVectorPort {

    override fun save(request: SaveKeywordReqeust) {
        val points = request.keywords.map { sentence ->
            Points.PointStruct.newBuilder()
                .setId(id(uuidGenerator.generate())) // 각 문장마다 새로운 ID
                .setVectors(vectors(sentence.vector.map { num -> num.toFloat() }))
                .putAllPayload(
                    mapOf(
                        "memberId" to value(request.authorId),
                        "diaryId" to value(request.diaryId),
                        "keyword" to value(sentence.content)
                    )
                )
                .build()
        }

        if (points.isNotEmpty()) {
            qdrantClient.upsertAsync(collectionName, points)
        }
    }

    override fun searchByKeyword(query: SearchKeywordQuery): List<SearchKeywordResult> {

        val filterCondition = Common.Filter.newBuilder()
            .addMust(match("memberId", query.authorId))
            .build()

        val searchRequest = Points.SearchPoints.newBuilder()
            .setCollectionName(collectionName)
            .addAllVector(query.vector.map { it.toFloat() })
            .setFilter(filterCondition)
            .setWithPayload(enable(true))
            .setLimit(10)
            .build()

        val scoredPoints = qdrantClient.searchAsync(searchRequest).get()

        return scoredPoints.map { point -> SearchKeywordResult(
            keyword = point.payloadMap["keyword"]?.stringValue ?: "",
            diaryId = point.payloadMap["diaryId"]?.integerValue ?: 0L
        ) }
    }

}
