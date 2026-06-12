package b1a4.harudew.diary.application.port.`in`

import b1a4.harudew.diary.adapter.dto.response.DiaryAnalysisResult
import b1a4.harudew.diary.adapter.dto.response.BookmarkToggleResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryDateResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryDetailResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryEmotionScoreResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryMapResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPageResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPhotosResponse
import b1a4.harudew.diary.adapter.dto.response.DiarySearchResponse
import b1a4.harudew.diary.adapter.dto.response.WrittenDaysResponse
import b1a4.harudew.member.domain.Member
import java.time.LocalDate

interface DiaryQueryUseCase {

    fun delete(diaryId: Long, member: Member)

    fun toggleBookmark(diaryId: Long, member: Member): BookmarkToggleResponse

    fun findById(diaryId: Long, member: Member): DiaryAnalysisResult

    fun findJsonById(diaryId: Long, member: Member): DiaryDetailResponse

    fun findByDate(member: Member, date: LocalDate): DiaryDateResponse

    fun search(member: Member, query: String): DiarySearchResponse

    fun findAll(member: Member, cursor: Long?, limit: Int): DiaryPageResponse

    fun findBookmark(member: Member, page: Int, limit: Int): DiaryPageResponse

    fun findPhotos(member: Member, cursor: Long?, limit: Int): DiaryPhotosResponse

    fun findWrittenDays(member: Member, year: Int, month: Int): WrittenDaysResponse

    fun findMap(member: Member): DiaryMapResponse

    fun findEmotionScores(diaryId: Long, member: Member, period: Int): DiaryEmotionScoreResponse
}
