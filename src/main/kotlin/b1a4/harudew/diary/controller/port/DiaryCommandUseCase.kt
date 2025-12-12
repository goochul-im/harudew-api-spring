package b1a4.harudew.diary.controller.port

import b1a4.harudew.diary.controller.port.dto.`in`.CreateDiaryCommand

interface DiaryCommandUseCase {

    fun create(command : CreateDiaryCommand)

    fun delete(diaryId: Long)

    fun toggleBookmark(diaryId: Long)

}
