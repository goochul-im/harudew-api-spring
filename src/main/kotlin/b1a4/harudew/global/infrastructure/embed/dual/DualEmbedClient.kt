package b1a4.harudew.global.infrastructure.embed.dual

import io.netty.util.concurrent.Promise
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class DualEmbedClient(
    @Value("\${embed.dual.url}") // application.yml의 설정값 읽기
    private val url: String,
    restClientBuilder: RestClient.Builder
) : DualEmbedClientPort {

    private val restClient = restClientBuilder.build()

    override fun embedQuery(text: String): List<Double> {
        val response = restClient.post()
            .uri(url)
            .body(EmbedRawRequest(text, "query"))
            .retrieve()
            .body(EmbedRawResponse::class.java)

        return response!!.embedding
    }

    override fun embedPassage(text: String) : List<Double> {
        val response = restClient.post()
            .uri(url)
            .body(EmbedRawRequest(text, "passage"))
            .retrieve()
            .body(EmbedRawResponse::class.java)

        return response!!.embedding
    }

    private data class EmbedRawRequest(
        val text: String,
        val prefix: String
    )

    private data class EmbedRawResponse(
        val embedding: List<Double>
    )

}
