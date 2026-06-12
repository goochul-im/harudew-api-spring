package b1a4.harudew.todo.adapter.out.persistence.entity

import b1a4.harudew.todo.domain.DiaryTodo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "diary_todo")
class DiaryTodoEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "diary_id", nullable = false)
    val diaryId: Long,

    @Column(name = "author_id", nullable = false)
    val authorId: String,

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    val content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDate

) {

    fun toDomain() = DiaryTodo(
        id = this.id,
        diaryId = this.diaryId,
        authorId = this.authorId,
        content = this.content,
        createdAt = this.createdAt
    )

    companion object {
        fun fromDomain(domain: DiaryTodo) = DiaryTodoEntity(
            id = domain.id,
            diaryId = domain.diaryId,
            authorId = domain.authorId,
            content = domain.content,
            createdAt = domain.createdAt
        )
    }
}
