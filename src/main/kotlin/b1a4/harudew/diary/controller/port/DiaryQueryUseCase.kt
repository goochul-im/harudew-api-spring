package b1a4.harudew.diary.controller.port

import b1a4.harudew.diary.controller.dto.out.DiaryAnalysisResponse
import b1a4.harudew.diary.controller.dto.out.DiaryResponse
import b1a4.harudew.diary.controller.dto.out.DiaryDetailResponse
import b1a4.harudew.diary.controller.dto.out.DiaryPageResponse
import b1a4.harudew.diary.controller.dto.out.DiarySearchResponse
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
