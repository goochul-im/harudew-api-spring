package b1a4.harudew.diary.controller.port

import b1a4.harudew.diary.controller.port.dto.out.DiaryAnalysisResponse
import b1a4.harudew.diary.controller.port.dto.out.DiaryJsonResponse
import b1a4.harudew.diary.domain.Diary
import b1a4.harudew.member.domain.Member
import java.time.LocalDate

interface DiaryQueryUseCase {

    fun findById(diaryId: Long, member: Member): DiaryAnalysisResponse

    fun findJsonById(diaryId: Long, member: Member): DiaryJsonResponse

    fun findInfoByDate(diaryId: Long, member: Member, date: LocalDate): DiaryInfoResponse

    fun search(member: Member, query: String): DiarySearchResponse

    fun findAll(member: Member): DiaryPageResponse

    fun findBookmark(member: Member): DiaryPageResponse

}
