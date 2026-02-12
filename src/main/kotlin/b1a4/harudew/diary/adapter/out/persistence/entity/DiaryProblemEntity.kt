package b1a4.harudew.diary.adapter.out.persistence.entity

import b1a4.harudew.diary.domain.model.DiaryProblem
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "diary_problem")
class DiaryProblemEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "diary_id", nullable = false)
    val diaryId: Long,

    @Column(name = "activity_name", nullable = false)
    val activityName: String, // TODO: Activity 엔티티 구현 후 FK로 전환

    @Column(name = "situation", columnDefinition = "TEXT", nullable = false)
    val situation: String,

    @Column(name = "approach", columnDefinition = "TEXT", nullable = false)
    val approach: String,

    @Column(name = "outcome", columnDefinition = "TEXT", nullable = false)
    val outcome: String,

    @Column(name = "conflict_response_code", nullable = false)
    val conflictResponseCode: String

) {

    fun toDomain() = DiaryProblem(
        id = this.id,
        diaryId = this.diaryId,
        activityName = this.activityName,
        situation = this.situation,
        approach = this.approach,
        outcome = this.outcome,
        conflictResponseCode = this.conflictResponseCode
    )

    companion object {
        fun fromDomain(domain: DiaryProblem) = DiaryProblemEntity(
            id = domain.id,
            diaryId = domain.diaryId,
            activityName = domain.activityName,
            situation = domain.situation,
            approach = domain.approach,
            outcome = domain.outcome,
            conflictResponseCode = domain.conflictResponseCode
        )
    }
}
