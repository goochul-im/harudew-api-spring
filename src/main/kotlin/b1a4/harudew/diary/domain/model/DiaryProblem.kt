package b1a4.harudew.diary.domain.model

data class DiaryProblem(
    val id: Long? = null,
    val diaryId: Long,
    val activityName: String, // TODO: Activity 엔티티 구현 후 FK로 전환
    val situation: String,
    val approach: String,
    val outcome: String,
    val conflictResponseCode: String
)
