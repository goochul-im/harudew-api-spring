package b1a4.harudew.global.infrastructure.cluster

import com.fasterxml.jackson.annotation.JsonProperty

interface ClusterClientPort {

    fun getClusters(request: ClusterRequest): ClusterResponse

}

data class ClusterRequest(
    val sentences: List<SentenceItem>,

    @get:JsonProperty("max_clusters") // 필드명은 CamelCase, JSON은 snake_case
    val maxClusters: Int = 20
) {
    data class SentenceItem(
        val id: Int,
        val text: String
    )
}

// application/port/out/dto/ClusterResponse.kt
data class ClusterResponse(
    @JsonProperty("optimal_clusters")
    val optimalClusters: Int,

    @JsonProperty("silhouette_score")
    val silhouetteScore: Double,

    @JsonProperty("total_sentences")
    val totalSentences: Int,

    val clusters: List<ClusterGroup>
) {
    data class ClusterGroup(
        @JsonProperty("cluster_id")
        val clusterId: Int,

        @JsonProperty("cluster_size")
        val clusterSize: Int,

        @JsonProperty("representative_sentence")
        val representativeSentence: SentenceItem,

        val sentences: List<SentenceItem>
    )

    data class SentenceItem(
        val id: Int,
        val text: String
    )
}
