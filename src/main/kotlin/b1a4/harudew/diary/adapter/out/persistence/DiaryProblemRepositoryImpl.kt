package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.adapter.out.persistence.entity.DiaryProblemEntity
import b1a4.harudew.diary.application.port.out.DiaryProblemRepository
import b1a4.harudew.diary.domain.model.DiaryProblem
import org.springframework.stereotype.Repository

@Repository
class DiaryProblemRepositoryImpl(
    private val diaryProblemJpaRepository: DiaryProblemJpaRepository
) : DiaryProblemRepository {

    override fun saveAll(diaryProblems: List<DiaryProblem>): List<DiaryProblem> {
        val entities = diaryProblems.map { DiaryProblemEntity.fromDomain(it) }
        return diaryProblemJpaRepository.saveAll(entities).map { it.toDomain() }
    }
}
