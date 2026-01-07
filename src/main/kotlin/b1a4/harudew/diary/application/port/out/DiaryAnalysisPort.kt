package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.application.port.out.dto.DiaryAnalysisResponse

interface DiaryAnalysisPort {

    fun getAnalysis(content: String): DiaryAnalysisResponse?

}
