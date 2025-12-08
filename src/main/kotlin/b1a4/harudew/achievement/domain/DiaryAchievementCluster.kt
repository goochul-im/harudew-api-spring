package b1a4.harudew.achievement.domain

/**
 * 성취 클러스터 도메인
 * @param id
 * @param author 소유자
 * @param clusterCount 클러스터에 속한 성취 카운트
 * @param label 클러스터 메인 레이블
 * @param centroid 클러스터 위치
 */
class DiaryAchievementCluster(
    val id: Long,
    val author: String,
    val clusterCount: Int,
    val label: String,
    val centroid: List<Number>,
) {
}
