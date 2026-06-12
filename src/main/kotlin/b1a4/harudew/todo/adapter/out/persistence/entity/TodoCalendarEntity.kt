package b1a4.harudew.todo.adapter.out.persistence.entity

import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import b1a4.harudew.todo.adapter.dto.response.TodoCalendarResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "todo_calendar")
class TodoCalendarEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: MemberEntity,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(name = "is_completed", nullable = false)
    var isComplete: Boolean = false,

    @Column(nullable = false)
    var date: LocalDate
) {
    fun toResponse() = TodoCalendarResponse(
        id = requireNotNull(id),
        content = content,
        isComplete = isComplete,
        date = date
    )
}
