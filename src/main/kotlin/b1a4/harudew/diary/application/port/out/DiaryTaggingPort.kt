package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.application.port.out.dto.AiDiaryTaggingResponse

interface DiaryTaggingPort {

    fun tag(content: String): AiDiaryTaggingResponse?

}
