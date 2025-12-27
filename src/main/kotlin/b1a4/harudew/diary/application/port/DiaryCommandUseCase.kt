package b1a4.harudew.diary.application.port

import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand

interface DiaryCommandUseCase {

    fun create(command : CreateDiaryCommand)

    fun delete(diaryId: Long)

    fun toggleBookmark(diaryId: Long)

}
