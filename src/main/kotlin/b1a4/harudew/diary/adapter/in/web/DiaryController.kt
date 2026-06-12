package b1a4.harudew.diary.adapter.`in`.web

import b1a4.harudew.auth.annotation.MemberId
import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand
import b1a4.harudew.diary.application.port.`in`.DiaryCommandUseCase
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@RestController
@RequestMapping("/diary")
class DiaryController(
    private val diaryCommandUseCase: DiaryCommandUseCase
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @MemberId memberId: String,
        @RequestParam content: String,
        @RequestParam writtenDate: LocalDate,
        @RequestParam(required = false, defaultValue = "NONE") weather: String,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @RequestParam(required = false, name = "photo") photos: List<MultipartFile>?,
        @RequestParam(required = false, name = "audios") audios: List<MultipartFile>?
    ): CreateDiaryResponse = runBlocking {
        val diaryId = diaryCommandUseCase.create(
            authorId = memberId,
            command = CreateDiaryCommand(
                writtenDate = writtenDate,
                content = content,
                weather = weather,
                latitude = latitude,
                longitude = longitude
            ),
            photos = photos,
            audios = audios
        )
        CreateDiaryResponse(id = diaryId)
    }
}

data class CreateDiaryResponse(
    val id: Long
)
