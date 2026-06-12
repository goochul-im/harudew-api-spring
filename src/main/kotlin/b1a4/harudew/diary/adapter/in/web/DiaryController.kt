package b1a4.harudew.diary.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.auth.annotation.MemberId
import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand
import b1a4.harudew.diary.adapter.dto.response.BookmarkToggleResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryAnalysisResult
import b1a4.harudew.diary.adapter.dto.response.DiaryDateResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryDetailResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryEmotionScoreResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPageResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPhotosResponse
import b1a4.harudew.diary.adapter.dto.response.DiarySearchResponse
import b1a4.harudew.diary.adapter.dto.response.WrittenDaysResponse
import b1a4.harudew.diary.application.port.`in`.DiaryCommandUseCase
import b1a4.harudew.diary.application.port.`in`.DiaryQueryUseCase
import b1a4.harudew.member.domain.Member
import kotlinx.coroutines.runBlocking
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@RestController
@RequestMapping("/diary")
class DiaryController(
    private val diaryCommandUseCase: DiaryCommandUseCase,
    private val diaryQueryUseCase: DiaryQueryUseCase
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

    @GetMapping("/home")
    fun home(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "0") cursor: Long?,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): DiaryPageResponse = diaryQueryUseCase.findAll(member, cursor, limit)

    @GetMapping("/date")
    fun byDate(
        @CurrentMember member: Member,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): DiaryDateResponse = diaryQueryUseCase.findByDate(member, date)

    @GetMapping("/date/{date}")
    fun byDatePath(
        @CurrentMember member: Member,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): DiaryDateResponse = diaryQueryUseCase.findByDate(member, date)

    @GetMapping("/search")
    fun search(
        @CurrentMember member: Member,
        @RequestParam("q") query: String
    ): DiarySearchResponse = diaryQueryUseCase.search(member, query)

    @GetMapping("/bookmark")
    fun bookmarks(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): DiaryPageResponse = diaryQueryUseCase.findBookmark(member, page, limit)

    @PatchMapping("/bookmark/{id}")
    fun toggleBookmark(
        @CurrentMember member: Member,
        @PathVariable("id") diaryId: Long
    ): BookmarkToggleResponse = diaryQueryUseCase.toggleBookmark(diaryId, member)

    @GetMapping("/photos")
    fun photos(
        @CurrentMember member: Member,
        @RequestParam(required = false, defaultValue = "0") cursor: Long?,
        @RequestParam(required = false, defaultValue = "10") limit: Int
    ): DiaryPhotosResponse = diaryQueryUseCase.findPhotos(member, cursor, limit)

    @GetMapping("/writtenDays")
    fun writtenDays(
        @CurrentMember member: Member,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): WrittenDaysResponse = diaryQueryUseCase.findWrittenDays(member, year, month)

    @GetMapping("/date/emotion/{id}")
    fun emotionScores(
        @CurrentMember member: Member,
        @PathVariable("id") diaryId: Long,
        @RequestParam(required = false, defaultValue = "7") period: Int
    ): DiaryEmotionScoreResponse = diaryQueryUseCase.findEmotionScores(diaryId, member, period)

    @GetMapping("/json/{id}")
    fun jsonDetail(
        @CurrentMember member: Member,
        @PathVariable("id") diaryId: Long
    ): DiaryDetailResponse = diaryQueryUseCase.findJsonById(diaryId, member)

    @GetMapping("/{id}")
    fun detail(
        @CurrentMember member: Member,
        @PathVariable("id") diaryId: Long
    ): DiaryAnalysisResult = diaryQueryUseCase.findById(diaryId, member)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @CurrentMember member: Member,
        @PathVariable("id") diaryId: Long
    ) {
        diaryQueryUseCase.delete(diaryId, member)
    }
}

data class CreateDiaryResponse(
    val id: Long
)
