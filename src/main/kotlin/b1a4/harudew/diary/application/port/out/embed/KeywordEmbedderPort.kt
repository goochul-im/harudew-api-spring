package b1a4.harudew.diary.application.port.out.embed

interface KeywordEmbedderPort {

    fun embed(content: String): List<Double>

}
