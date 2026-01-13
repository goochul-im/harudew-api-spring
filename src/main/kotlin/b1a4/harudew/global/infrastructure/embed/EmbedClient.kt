package b1a4.harudew.global.infrastructure.embed

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class EmbedClient(
    @Value("\${embed.simple.url}") // application.yml의 설정값 읽기
    private val url: String,
    restClientBuilder: RestClient.Builder
) : EmbedClientPort {

    private val restClient = restClientBuilder.build()

    override fun embed(content: String): List<Double> {
        val cleanText = content.trim()
        if (cleanText.isEmpty()) {
            throw RuntimeException()
        }

        val response = restClient.post()
            .uri(url)
            .body(EmbedRawRequest(cleanText))
            .retrieve()
            .body(EmbedRawResponse::class.java)

        return response!!.embedding
    }

    data class EmbedRawRequest(
        val text: String
    )

    data class EmbedRawResponse(
        val success: Boolean,
        val embedding: List<Double>,
        val dimension: Int?,
        val error: String?
    )

}
