package b1a4.harudew.recommend.adapter.out.persistence

import b1a4.harudew.recommend.adapter.out.persistence.entity.RoutineEntity
import b1a4.harudew.recommend.domain.RoutineType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface RoutineJpaRepository : JpaRepository<RoutineEntity, Long> {

    @Query("select r from RoutineEntity r where r.member.id = :memberId and r.routineType = :type order by r.id desc")
    fun findByMemberAndType(
        @Param("memberId") memberId: String,
        @Param("type") type: RoutineType
    ): List<RoutineEntity>

    @Query("select r from RoutineEntity r where r.member.id = :memberId and r.isTrigger = true order by r.id desc")
    fun findTriggers(@Param("memberId") memberId: String): List<RoutineEntity>

    @Query("select r from RoutineEntity r where r.id = :id and r.member.id = :memberId")
    fun findOwnedById(
        @Param("id") id: Long,
        @Param("memberId") memberId: String
    ): Optional<RoutineEntity>

    fun existsByMember_IdAndRoutineTypeAndContent(memberId: String, routineType: RoutineType, content: String): Boolean
}
