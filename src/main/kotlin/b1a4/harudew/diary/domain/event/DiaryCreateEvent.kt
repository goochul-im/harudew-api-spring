package b1a4.harudew.diary.domain.event

import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.global.event.DomainEvent

data class DiaryCreateEvent(
    val diaryId: Long,
    val analysisResult : DiaryAnalysisResponse
) : DomainEvent(
    aggregateType = "Diary",
    aggregateId = diaryId.toString()
)
