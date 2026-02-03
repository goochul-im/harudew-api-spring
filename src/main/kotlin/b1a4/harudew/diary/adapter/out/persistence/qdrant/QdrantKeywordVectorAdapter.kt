package b1a4.harudew.diary.adapter.out.persistence.qdrant

import b1a4.harudew.diary.application.port.out.vector.keyword.KeywordVectorPort
import b1a4.harudew.diary.application.port.out.vector.keyword.SaveKeywordRequest
import b1a4.harudew.diary.application.port.out.vector.keyword.SearchKeywordQuery
import b1a4.harudew.diary.application.port.out.vector.keyword.SearchKeywordResponse
import b1a4.harudew.global.infrastructure.qdrant.QdrantClientPort
import b1a4.harudew.global.infrastructure.qdrant.QdrantPointData
import b1a4.harudew.global.infrastructure.qdrant.QdrantSearchQuery
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class QdrantKeywordVectorAdapter(
    private val qdrantClientPort: QdrantClientPort,
    @Value("\${qdrant.collection.keyword_name}")
    private val collectionName: String
) : KeywordVectorPort {

    override fun save(request: SaveKeywordRequest) {
        val points = request.keywords.map { keyword ->
            QdrantPointData(
                vector = keyword.vector,
                payload = mapOf(
                    "memberId" to request.authorId,
                    "diaryId" to request.diaryId,
                    "keyword" to keyword.content
                )
            )
        }
        qdrantClientPort.upsertAsync(collectionName, points)
    }

    override fun searchByKeyword(query: SearchKeywordQuery): List<SearchKeywordResponse> {
        val searchQuery = QdrantSearchQuery(
            collectionName = collectionName,
            vector = query.vector,
            filters = mapOf("memberId" to query.authorId),
            limit = 10
        )

        return qdrantClientPort.search(searchQuery).map { result ->
            SearchKeywordResponse(
                keyword = result.getString("keyword") ?: "",
                diaryId = result.getLong("diaryId") ?: 0L
            )
        }
    }
}
