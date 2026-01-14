package b1a4.harudew.diary.application.port.out.rerank

interface RerankPort {

    fun rerank(request: RerankRequest) : List<RerankResult>

}
