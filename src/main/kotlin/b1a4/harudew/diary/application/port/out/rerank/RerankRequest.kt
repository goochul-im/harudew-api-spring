package b1a4.harudew.diary.application.port.out.rerank

import b1a4.harudew.global.infrastructure.rerank.CandidateRequest

data class RerankRequest(
    val query: String,
    val candidates: List<Candidates>
)

data class Candidates(
    val rdbID: Long,
    val text: String
)
