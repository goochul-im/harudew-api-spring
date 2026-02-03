package b1a4.harudew.diary.application.port.`in`

import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand
import org.springframework.web.multipart.MultipartFile

interface DiaryCommandUseCase {

    suspend fun create(authorId: String ,command : CreateDiaryCommand, photos: List<MultipartFile>?)

    fun delete(diaryId: Long)

    fun toggleBookmark(diaryId: Long)

}
