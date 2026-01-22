package b1a4.harudew.global.infrastructure.cluster

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ClusterClientProvider(
    @Value("\${cluster.model.url}") // application.yml의 설정값 읽기
    private val url: String,
    restClientBuilder: RestClient.Builder
) : ClusterClientPort {

    private val restClient = restClientBuilder.build()

    override fun getClusters(request: ClusterRequest): ClusterResponse {

        val response = restClient.post()
            .uri(url)
            .body(request)
            .retrieve()
            .body(ClusterResponse::class.java)

        return response ?: throw RuntimeException()
    }
}
