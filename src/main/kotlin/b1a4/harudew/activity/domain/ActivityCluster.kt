package b1a4.harudew.activity.domain

import b1a4.harudew.member.domain.Member

class ActivityCluster(
    val id: Long,
    val author: Member,
    val clusteredCount: Int,
    val label: String,
    val centroid: List<Number>,
)
