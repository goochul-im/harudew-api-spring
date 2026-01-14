package b1a4.harudew.diary.application.port.out.vector.keyword

interface KeywordVectorPort {

    fun save(request: SaveKeywordReqeust)

    fun searchByKeyword(keyword: String, authorId: Long)

}
