package b1a4.harudew.activity.domain

import b1a4.harudew.diary.domain.model.Diary
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @param id
 * @param content 내용
 * @param vector Qdrant 벡터
 * @param createdAt 생성 시간
 * @param diary 이 활동이 있는 일기
 * @param strength 이 활동에 대한 강점
 * @param weakness 이 활동에 대한 약점
 * @param cluster 클러스터
 */
class Activity(
    val id: Long,
    val content: String,
    val vector: List<Number>,
    val createdAt: LocalDateTime,
    val diary: Diary,
    val strength: String?,
    val weakness: String?,
    val cluster: ActivityCluster,
) {
}
