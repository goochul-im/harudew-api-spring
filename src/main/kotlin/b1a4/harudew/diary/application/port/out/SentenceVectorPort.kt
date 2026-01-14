package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.application.port.out.dto.SaveSentenceRequest

interface SentenceVectorPort {

    fun save(request: SaveSentenceRequest)

}
