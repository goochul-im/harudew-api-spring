package b1a4.harudew.diary.application.port.out.extract

interface DiaryKeywordExtracterPort {

    fun extract(content: String): KeywordExtractResponse?

}
