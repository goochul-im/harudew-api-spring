package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.application.port.out.dto.SaveKeywordReqeust

interface KeywordVectorPort {

    fun save(request: SaveKeywordReqeust)

    fun searchByKeyword(keyword: String, authorId: Long)

}
