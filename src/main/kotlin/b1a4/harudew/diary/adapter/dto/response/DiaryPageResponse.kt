package b1a4.harudew.diary.adapter.dto.response

data class DiaryPageResponse(
    val item: DiaryHomeResponse,
    val hasMore: Boolean,
    val nextCursor: Int
)

data class DiaryHomeResponse(
    val diaries: List<DiaryResponse>,
    val continuousWritingDate: Int,
    val totalDiaryCount: Int,
    val emotionCountByMonth: Int
)
