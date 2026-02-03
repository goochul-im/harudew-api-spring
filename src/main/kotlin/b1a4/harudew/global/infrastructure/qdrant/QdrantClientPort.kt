package b1a4.harudew.global.infrastructure.qdrant

interface QdrantClientPort {

    fun upsertAsync(collectionName: String, points: List<QdrantPointData>)

    fun search(query: QdrantSearchQuery): List<QdrantSearchResult>

    fun deleteByFilter(collectionName: String, filter: Map<String, Any>)
}
