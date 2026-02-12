package b1a4.harudew.diary.domain.model

data class DiaryReflection(
    val id: Long? = null,
    val diaryId: Long,
    val shortcomings: List<String>,
    val tomorrowMindset: String?
)
