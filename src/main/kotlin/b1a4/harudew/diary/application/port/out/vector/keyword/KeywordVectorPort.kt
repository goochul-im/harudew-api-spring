package b1a4.harudew.diary.application.port.out.vector.keyword

interface KeywordVectorPort {

    fun save(request: SaveKeywordRequest)

    fun searchByKeyword(query: SearchKeywordQuery) : List<SearchKeywordResponse>

}
