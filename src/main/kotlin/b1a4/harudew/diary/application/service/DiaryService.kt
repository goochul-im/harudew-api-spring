package b1a4.harudew.diary.application.service

import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand
import b1a4.harudew.diary.adapter.dto.response.DiaryAnalysisResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryDetailResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPageResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryResponse
import b1a4.harudew.diary.adapter.dto.response.DiarySearchResponse
import b1a4.harudew.diary.application.port.`in`.DiaryCommandUseCase
import b1a4.harudew.diary.application.port.`in`.DiaryQueryUseCase
import b1a4.harudew.member.domain.Member
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class DiaryService(

) : DiaryCommandUseCase, DiaryQueryUseCase {

    override fun create(command: CreateDiaryCommand) {
        TODO("Not yet implemented")
    }

    override fun delete(diaryId: Long) {
        TODO("Not yet implemented")
    }

    override fun toggleBookmark(diaryId: Long) {
        TODO("Not yet implemented")
    }

    override fun findById(
        diaryId: Long,
        member: Member
    ): DiaryAnalysisResponse {
        TODO("Not yet implemented")
    }

    override fun findJsonById(
        diaryId: Long,
        member: Member
    ): DiaryDetailResponse {
        TODO("Not yet implemented")
    }

    override fun findInfoByDate(
        diaryId: Long,
        member: Member,
        date: LocalDate
    ): DiaryResponse {
        TODO("Not yet implemented")
    }

    override fun search(
        member: Member,
        query: String
    ): DiarySearchResponse {
        TODO("Not yet implemented")
    }

    override fun findAll(member: Member): DiaryPageResponse {
        TODO("Not yet implemented")
    }

    override fun findBookmark(member: Member): DiaryPageResponse {
        TODO("Not yet implemented")
    }
}
