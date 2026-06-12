package b1a4.harudew.recommend.application.service

import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.recommend.adapter.dto.response.RecommendRoutineResponse
import b1a4.harudew.recommend.adapter.dto.response.RoutineResponse
import b1a4.harudew.recommend.adapter.dto.response.RoutineToggleResponse
import b1a4.harudew.recommend.adapter.out.persistence.RoutineJpaRepository
import b1a4.harudew.recommend.adapter.out.persistence.entity.RoutineEntity
import b1a4.harudew.recommend.domain.RoutineType
import org.springframework.stereotype.Service

@Service
class RoutineService(
    private val routineJpaRepository: RoutineJpaRepository,
    private val memberJpaRepository: MemberJpaRepository,
    private val diaryRepository: DiaryRepository
) {

    fun findByType(memberId: String, type: RoutineType): List<RoutineResponse> =
        routineJpaRepository.findByMemberAndType(memberId, type).ifEmpty {
            seedDefaults(memberId, type)
        }.map { it.toResponse() }

    fun create(memberId: String, type: RoutineType, content: String): RoutineResponse {
        val member = memberJpaRepository.getReferenceById(memberId)
        return routineJpaRepository.save(
            RoutineEntity(
                member = member,
                routineType = type,
                content = content
            )
        ).toResponse()
    }

    fun findTriggers(memberId: String): List<RoutineResponse> =
        routineJpaRepository.findTriggers(memberId).map { it.toResponse() }

    fun toggle(memberId: String, id: Long): RoutineToggleResponse {
        val routine = routineJpaRepository.findOwnedById(id, memberId)
            .orElseThrow { NoSuchElementException("routine을 찾을 수 없습니다. id=$id") }
        routine.isTrigger = !routine.isTrigger
        val saved = routineJpaRepository.save(routine)
        return RoutineToggleResponse(requireNotNull(saved.id), saved.isTrigger)
    }

    fun delete(memberId: String, id: Long) {
        val routine = routineJpaRepository.findOwnedById(id, memberId)
            .orElseThrow { NoSuchElementException("routine을 찾을 수 없습니다. id=$id") }
        routineJpaRepository.delete(routine)
    }

    fun recommend(memberId: String, diaryId: Long): RecommendRoutineResponse {
        diaryRepository.findByIdAndAuthorId(diaryId, memberId)
        val routine = routineJpaRepository.findByMemberAndType(memberId, RoutineType.STRESS).firstOrNull()
            ?: seedDefaults(memberId, RoutineType.STRESS).first()
        return RecommendRoutineResponse(
            id = requireNotNull(routine.id),
            type = routine.routineType,
            content = routine.content
        )
    }

    private fun seedDefaults(memberId: String, type: RoutineType): List<RoutineEntity> {
        val member = memberJpaRepository.getReferenceById(memberId)
        val defaults = when (type) {
            RoutineType.STRESS -> listOf("3분 동안 천천히 호흡하기", "가벼운 스트레칭 하기")
            RoutineType.ANXIETY -> listOf("지금 걱정되는 일을 한 문장으로 적기", "주변의 안정적인 물건 5개 찾기")
            RoutineType.DEPRESSION -> listOf("햇빛을 보며 10분 걷기", "오늘 한 작은 성취 하나 적기")
        }
        return routineJpaRepository.saveAll(
            defaults.map { content ->
                RoutineEntity(member = member, routineType = type, content = content)
            }
        )
    }
}
