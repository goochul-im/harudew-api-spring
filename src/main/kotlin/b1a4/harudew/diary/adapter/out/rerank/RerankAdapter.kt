package b1a4.harudew.diary.adapter.out.rerank

import b1a4.harudew.diary.adapter.exception.RerankFailedException
import b1a4.harudew.diary.application.port.out.RerankPort
import b1a4.harudew.diary.application.port.out.dto.RerankRequest
import b1a4.harudew.diary.application.port.out.dto.RerankResult
import b1a4.harudew.global.infrastructure.rerank.CandidateRequest
import b1a4.harudew.global.infrastructure.rerank.CrossEncoderRerankClient
import b1a4.harudew.global.infrastructure.rerank.RerankClientPort
import org.springframework.stereotype.Component

@Component
class RerankAdapter(
    private val rerankClient: RerankClientPort
) : RerankPort {

    override fun rerank(request: RerankRequest): List<RerankResult> {
        return try {
            rerankClient.fetchRerank(
                request.query,
                request.candidates.map {
                    CandidateRequest(
                        it.rdbID,
                        it.text
                    )
                }
            )
        } catch (e: Exception) {
            throw RerankFailedException(
                e,
                mapOf(
                    "query" to request.query,
                    "candidates" to request.candidates
                )
            )
        }
    }

}
