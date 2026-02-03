package b1a4.harudew.diary.application.port.`in`

import java.time.LocalDate

interface DiaryRagUseCase {

    fun preprocessing(command: DiaryRagPreprocessingCommand)

}

data class DiaryRagPreprocessingCommand(
    val content: String,
    val diaryId: Long,
    val authorId: String,
    val writtenDate: LocalDate
)
