package b1a4.harudew.diary.application.port.`in`

import java.time.LocalDate

interface DiaryPreprocessingUseCase {

    fun ragPreprocessing(command: DiaryRagPreprocessingCommand)
    fun keywordPreprocessing(command: DiaryKeywordPreprocessingCommand)

}

data class DiaryKeywordPreprocessingCommand(
    val content: String,
    val diaryId: Long,
    val authorId: String
)

data class DiaryRagPreprocessingCommand(
    val content: String,
    val diaryId: Long,
    val authorId: String,
    val writtenDate: LocalDate
)
