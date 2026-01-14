package b1a4.harudew.global.infrastructure.rerank

import b1a4.harudew.diary.application.port.out.rerank.RerankResult

interface RerankClientPort {

    fun fetchRerank(query:String, candidates: List<CandidateRequest>) : List<RerankResult>

}
