package b1a4.harudew.diary.application.port.out.tagging

interface DiaryTaggingPort {

    fun tag(content: String): AiDiaryTaggingResponse

}
