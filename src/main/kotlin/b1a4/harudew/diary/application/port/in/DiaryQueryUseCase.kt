package b1a4.harudew.diary.application.port.`in`

import b1a4.harudew.diary.adapter.dto.response.DiaryAnalysisResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryDetailResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPageResponse
import b1a4.harudew.diary.adapter.dto.response.DiarySearchResponse
import b1a4.harudew.member.domain.Member
import java.time.LocalDate

interface DiaryQueryUseCase {

    fun findById(diaryId: Long, member: Member): DiaryAnalysisResponse

    fun findJsonById(diaryId: Long, member: Member): DiaryDetailResponse

    fun findInfoByDate(diaryId: Long, member: Member, date: LocalDate): DiaryResponse

    fun search(member: Member, query: String): DiarySearchResponse

    fun findAll(member: Member): DiaryPageResponse

    fun findBookmark(member: Member): DiaryPageResponse

}
