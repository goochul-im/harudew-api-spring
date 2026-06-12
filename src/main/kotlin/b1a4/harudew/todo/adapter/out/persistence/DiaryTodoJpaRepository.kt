package b1a4.harudew.todo.adapter.out.persistence

import b1a4.harudew.todo.adapter.out.persistence.entity.DiaryTodoEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryTodoJpaRepository : JpaRepository<DiaryTodoEntity, Long>
