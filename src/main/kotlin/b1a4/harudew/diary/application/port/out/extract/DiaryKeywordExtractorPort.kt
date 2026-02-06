package b1a4.harudew.diary.application.port.out.extract

interface DiaryKeywordExtractorPort {

    fun extract(content: String): KeywordExtractResponse

}
