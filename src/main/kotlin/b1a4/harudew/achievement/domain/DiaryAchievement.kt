package b1a4.harudew.achievement.domain

import b1a4.harudew.diary.domain.Diary

/**
 * 성취 도메인
 * @param id
 * @param content 내용
 * @param vector Qdrant 벡터
 * @param cluster 클러스터
 * @param diary 성취가 나타난 일기
 */
class DiaryAchievement(
    val id: Long,
    val content:String,
    val vector: List<Number>,
    val cluster: DiaryAchievementCluster,
    val diary: Diary
)
