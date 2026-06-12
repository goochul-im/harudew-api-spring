package b1a4.harudew.diary.adapter.dto.response

data class DiaryPageResponse(
    val item: DiaryHomeResponse,
    val hasMore: Boolean,
    val nextCursor: Long?
)

data class DiaryHomeResponse(
    val diaries: List<DiaryResponse>,
    val continuousWritingDate: Int,
    val totalDiaryCount: Int,
    val emotionCountByMonth: Int
)

data class DiaryDateResponse(
    val diaries: List<DiaryResponse>,
    val todayEmotions: List<EmotionAnalysisResponse> = emptyList(),
    val todayDiaries: List<DiaryResponse> = diaries
)

data class DiaryPhotoResponse(
    val diaryId: Long,
    val writtenDate: java.time.LocalDate,
    val photoPath: String,
    val content: String
)

data class DiaryPhotosResponse(
    val photos: List<DiaryPhotoResponse>,
    val hasMore: Boolean,
    val nextCursor: Long?
)

data class WrittenDaysResponse(
    val writtenDays: List<Int>
)

data class BookmarkToggleResponse(
    val id: Long,
    val isBookmarked: Boolean
)
