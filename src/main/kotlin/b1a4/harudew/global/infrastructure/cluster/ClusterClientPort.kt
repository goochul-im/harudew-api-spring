package b1a4.harudew.global.infrastructure.cluster

interface ClusterClientPort {

    fun getClusters(request: ClusterRequest): ClusterResponse

}

data class ClusterRequest(
    val sentences: List<SentenceItem>,
    val max_clusters: Int = 20
) {
    data class SentenceItem(
        val id: Int,
        val text: String
    )
}

data class ClusterResponse(
    val optimal_clusters: Int,
    val silhouette_score: Double,
    val total_sentences: Int,
    val clusters: List<ClusterGroup>
) {
    data class ClusterGroup(
        val cluster_id: Int,
        val cluster_size: Int,
        val representative_sentence: SentenceItem,
        val sentences: List<SentenceItem>
    )

    data class SentenceItem(
        val id: Int,
        val text: String
    )
}
