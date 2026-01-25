package b1a4.harudew.global.infrastructure.cluster

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Tag("integration")
class ClusterClientProviderIntegrationTest {

    @Autowired
    private lateinit var clusterClientProvider: ClusterClientProvider

    @Test
    @DisplayName("실제 클러스터링 서버에 요청하여 문장들이 정상적으로 클러스터링되는지 확인한다")
    fun clusterIntegrationTest() {
        // given
        val sentences = listOf(
            ClusterRequest.SentenceItem(1, "오늘 날씨가 정말 좋았다"),
            ClusterRequest.SentenceItem(2, "맑은 하늘이 기분 좋게 만들었다"),
            ClusterRequest.SentenceItem(3, "점심으로 맛있는 파스타를 먹었다"),
            ClusterRequest.SentenceItem(4, "저녁에는 치킨을 주문했다"),
            ClusterRequest.SentenceItem(5, "친구와 즐거운 대화를 나눴다"),
            ClusterRequest.SentenceItem(6, "오랜만에 만나서 반가웠다"),
            ClusterRequest.SentenceItem(7, "내일도 화창한 날씨가 예상된다"),
            ClusterRequest.SentenceItem(8, "음식이 너무 맛있어서 행복했다")
        )
        val request = ClusterRequest(sentences = sentences, maxClusters = 5)

        println("요청 내용:")
        println("- 문장 수: ${sentences.size}")
        println("- 최대 클러스터 수: ${request.maxClusters}")
        sentences.forEach { println("  [${it.id}] ${it.text}") }

        // when
        val result = clusterClientProvider.getClusters(request)

        // then
        println("\n응답 결과:")
        println("- 최적 클러스터 수: ${result.optimalClusters}")
        println("- 실루엣 점수: ${result.silhouetteScore}")
        println("- 총 문장 수: ${result.totalSentences}")
        println("- 클러스터 목록:")
        result.clusters.forEach { cluster ->
            println("  [클러스터 ${cluster.clusterId}] 크기: ${cluster.clusterSize}")
            println("    대표 문장: ${cluster.representativeSentence.text}")
            println("    포함 문장: ${cluster.sentences.map { it.text }}")
        }

        assertThat(result).isNotNull
        assertThat(result.totalSentences).isEqualTo(sentences.size)
        assertThat(result.optimalClusters).isGreaterThan(0)
        assertThat(result.optimalClusters).isLessThanOrEqualTo(request.maxClusters)
        assertThat(result.clusters).isNotEmpty
        assertThat(result.clusters.sumOf { it.clusterSize }).isEqualTo(sentences.size)
    }

    @Test
    @DisplayName("적은 수의 문장으로도 클러스터링이 동작하는지 확인한다")
    fun clusterWithFewSentencesTest() {
        // given
        val sentences = listOf(
            ClusterRequest.SentenceItem(1, "행복한 하루였다"),
            ClusterRequest.SentenceItem(2, "슬픈 일이 있었다"),
            ClusterRequest.SentenceItem(3, "기쁜 마음으로 잠들었다")
        )
        val request = ClusterRequest(sentences = sentences, maxClusters = 3)

        println("요청 내용:")
        sentences.forEach { println("  [${it.id}] ${it.text}") }

        // when
        val result = clusterClientProvider.getClusters(request)

        // then
        println("\n응답 결과:")
        println("- 최적 클러스터 수: ${result.optimalClusters}")
        println("- 클러스터: ${result.clusters.map { it.representativeSentence.text }}")

        assertThat(result).isNotNull
        assertThat(result.totalSentences).isEqualTo(sentences.size)
        assertThat(result.clusters).isNotEmpty
    }
}
