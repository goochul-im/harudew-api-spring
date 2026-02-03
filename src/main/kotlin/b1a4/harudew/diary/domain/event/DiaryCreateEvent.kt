package b1a4.harudew.diary.domain.event

import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.global.event.DomainEvent
import java.time.LocalDate

data class DiaryCreateEvent(
    val diaryId: Long,
    val content: String,
    val authorId: String,
    val writtenDate: LocalDate,
    val analysisResult : DiaryAnalysisResponse
) : DomainEvent(
    aggregateType = "Diary",
    aggregateId = diaryId.toString()
)
