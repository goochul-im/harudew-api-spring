package b1a4.harudew.diary.application.port.out

interface DiaryChunkPort {

    fun chunk(content: String): List<String>

}
