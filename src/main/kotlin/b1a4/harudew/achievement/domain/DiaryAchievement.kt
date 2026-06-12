package b1a4.harudew.achievement.domain

/**
 * 일기 분석에서 추출된 성취 원본.
 *
 * Stage 2에서는 분석 결과를 metaData에만 보존하지 않도록 diaryId 기반으로
 * 원본 텍스트를 먼저 저장하고, 벡터/클러스터 연결은 후속 성취 기능에서 채운다.
 */
data class DiaryAchievement(
    val id: Long? = null,
    val diaryId: Long,
    val content: String,
    val vector: List<Double> = emptyList(),
    val clusterId: Long? = null
)
