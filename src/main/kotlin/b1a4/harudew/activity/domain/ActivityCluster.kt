package b1a4.harudew.activity.domain

import b1a4.harudew.member.domain.Member

/**
 * @param id
 * @param author 활동 주인
 * @param clusteredCount 클러스터 카운터
 * @param label 레이블
 * @param centroid 벡터
 */
class ActivityCluster(
    val id: Long,
    val author: Member,
    val clusteredCount: Int,
    val label: String,
    val centroid: List<Number>,
)
