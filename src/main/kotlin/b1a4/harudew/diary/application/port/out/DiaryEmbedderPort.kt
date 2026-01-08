package b1a4.harudew.diary.application.port.out

interface DiaryEmbedderPort {

    fun embed(content: String): List<Double>

}
