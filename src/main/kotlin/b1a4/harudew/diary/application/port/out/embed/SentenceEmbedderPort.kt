package b1a4.harudew.diary.application.port.out.embed

interface SentenceEmbedderPort {

    fun embedPassage(content: String) : List<Double>

    fun embedQuery(content: String) : List<Double>

}
