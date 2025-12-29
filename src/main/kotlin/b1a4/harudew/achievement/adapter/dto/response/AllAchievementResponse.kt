package b1a4.harudew.achievement.adapter.dto.response

data class AllAchievementResponse(
    val achievement: List<AchievementResponse>
)

data class AchievementResponse(
    val id: Long,
    val label: String,
    val count: Int
)
