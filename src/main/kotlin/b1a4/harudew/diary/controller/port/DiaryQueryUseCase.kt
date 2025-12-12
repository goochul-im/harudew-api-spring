package b1a4.harudew.diary.controller.port

import b1a4.harudew.diary.controller.port.dto.out.DiaryAnalysisResponse
import b1a4.harudew.diary.domain.Diary

interface DiaryQueryUseCase {

    fun findById(id: Long): DiaryAnalysisResponse

    fun findJsonById(id: Long): DiaryJsonResponse

}
