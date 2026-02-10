package b1a4.harudew.diary.adapter.out.persistence.qdrant

import b1a4.harudew.diary.application.port.out.vector.sentence.SaveSentenceRequest
import b1a4.harudew.diary.application.port.out.vector.sentence.SearchSentenceQuery
import b1a4.harudew.diary.application.port.out.vector.sentence.SearchSentenceResponse
import b1a4.harudew.diary.application.port.out.vector.sentence.SentenceVectorPort
import b1a4.harudew.global.infrastructure.qdrant.QdrantClientPort
import b1a4.harudew.global.infrastructure.qdrant.QdrantPointData
import b1a4.harudew.global.infrastructure.qdrant.QdrantSearchQuery
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class QdrantSentenceAdapter(
    private val qdrantClientPort: QdrantClientPort,
    @Value("\${qdrant.collection.sentence_name}")
    private val collectionName: String
) : SentenceVectorPort {

    override fun save(request: SaveSentenceRequest) {
        val points = request.sentences.map { sentence ->
            QdrantPointData(
                vector = sentence.vector,
                payload = mapOf(
                    "memberId" to request.authorId,
                    "diary_Id" to request.diaryId,
                    "sentence" to sentence.content,
                    "date" to request.date
                )
            )
        }
        qdrantClientPort.upsertAsync(collectionName, points)
    }

    override fun search(query: SearchSentenceQuery): List<SearchSentenceResponse> {
        val searchQuery = QdrantSearchQuery(
            collectionName = collectionName,
            vector = query.vector,
            filters = mapOf("memberId" to query.authorId),
            limit = query.limit
        )

        return qdrantClientPort.search(searchQuery).map { result ->
            SearchSentenceResponse(
                sentence = result.getString("sentence") ?: "",
                diaryId = result.getLong("diary_Id") ?: 0L,
                date = result.getString("date")?.let { LocalDate.parse(it) }
                    ?: throw IllegalStateException("Missing date in sentence vector result"),
                score = result.score
            )
        }
    }
}
