package b1a4.harudew.diary.application.port.out.vector.sentence

interface SentenceVectorPort {

    fun save(request: SaveSentenceRequest)

    fun search(query: SearchSentenceQuery): List<SearchSentenceResponse>
}
