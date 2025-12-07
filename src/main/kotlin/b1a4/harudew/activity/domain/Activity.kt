package b1a4.harudew.activity.domain

import b1a4.harudew.diary.domain.Diary
import java.time.LocalDate
import java.time.LocalDateTime

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
