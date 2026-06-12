package b1a4.harudew.recommend.adapter.out.persistence.entity

import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import b1a4.harudew.recommend.adapter.dto.response.RoutineResponse
import b1a4.harudew.recommend.domain.RoutineType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "routine")
class RoutineEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,

    @Column(name = "routine_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val routineType: RoutineType,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(name = "is_trigger", nullable = false)
    var isTrigger: Boolean = false
) {
    fun toResponse() = RoutineResponse(
        id = requireNotNull(id),
        content = content,
        routineType = routineType,
        isTrigger = isTrigger
    )
}
