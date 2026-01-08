package b1a4.harudew.global.infrastructure.rerank

import b1a4.harudew.diary.application.port.out.dto.RerankResult
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class CrossEncoderRerankClient(
    @Value("\${embed.cross.url}")
    private val url: String,
    restClient: RestClient.Builder
) : RerankClientPort {

    private val restClient = restClient.build()

    override fun fetchRerank(
        query: String,
        candidates: List<CandidateRequest>
    ): List<RerankResult> {

        val response = restClient.post()
            .uri(url)
            .body(RerankFullReqeust(query, candidates))
            .retrieve()
            .body(object : ParameterizedTypeReference<List<RerankRawResponse>>() {})

        return response!!.map { RerankResult(it.id, it.text, it.score) }
    }

    private data class RerankFullReqeust(
        val query: String,
        val candidates: List<CandidateRequest>
    )

    private data class RerankRawResponse(
        val id: Long,
        val text: String,
        val score: Double
    )

}
