package b1a4.harudew.diary.application.port.out.vector

interface KeywordVectorPort {

    fun save(request: SaveKeywordReqeust)

    fun searchByKeyword(keyword: String, authorId: Long)

}
