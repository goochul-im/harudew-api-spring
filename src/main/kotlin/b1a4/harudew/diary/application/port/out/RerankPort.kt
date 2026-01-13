package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.application.port.out.dto.RerankRequest
import b1a4.harudew.diary.application.port.out.dto.RerankResult

interface RerankPort {

    fun rerank(request: RerankRequest) : List<RerankResult>

}
