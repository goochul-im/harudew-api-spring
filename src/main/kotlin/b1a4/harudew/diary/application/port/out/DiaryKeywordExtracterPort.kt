package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.application.port.out.dto.KeywordExtractResponse

interface DiaryKeywordExtracterPort {

    fun extract(content: String): KeywordExtractResponse?

}
