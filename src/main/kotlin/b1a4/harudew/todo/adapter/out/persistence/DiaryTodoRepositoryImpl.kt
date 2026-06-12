package b1a4.harudew.todo.adapter.out.persistence

import b1a4.harudew.todo.adapter.out.persistence.entity.DiaryTodoEntity
import b1a4.harudew.todo.application.port.out.DiaryTodoRepository
import b1a4.harudew.todo.domain.DiaryTodo
import org.springframework.stereotype.Repository

@Repository
class DiaryTodoRepositoryImpl(
    private val diaryTodoJpaRepository: DiaryTodoJpaRepository
) : DiaryTodoRepository {

    override fun saveAll(diaryTodos: List<DiaryTodo>): List<DiaryTodo> {
        val entities = diaryTodos.map { DiaryTodoEntity.fromDomain(it) }
        return diaryTodoJpaRepository.saveAll(entities).map { it.toDomain() }
    }
}
