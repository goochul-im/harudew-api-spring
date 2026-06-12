package b1a4.harudew.todo.adapter.out.persistence

import b1a4.harudew.todo.adapter.out.persistence.entity.TodoCalendarEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

interface TodoCalendarJpaRepository : JpaRepository<TodoCalendarEntity, Long> {

    @Query("select t from TodoCalendarEntity t where t.member.id = :memberId and t.date between :start and :end order by t.date asc, t.id asc")
    fun findByMemberAndDateBetween(
        @Param("memberId") memberId: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<TodoCalendarEntity>

    @Query("select t from TodoCalendarEntity t where t.member.id = :memberId and t.date = :date order by t.id asc")
    fun findByMemberAndDate(
        @Param("memberId") memberId: String,
        @Param("date") date: LocalDate
    ): List<TodoCalendarEntity>

    @Query("select t from TodoCalendarEntity t where t.id = :id and t.member.id = :memberId")
    fun findOwnedById(
        @Param("id") id: Long,
        @Param("memberId") memberId: String
    ): Optional<TodoCalendarEntity>
}
