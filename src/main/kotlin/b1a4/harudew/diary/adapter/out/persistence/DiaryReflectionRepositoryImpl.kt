package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.adapter.out.persistence.entity.DiaryReflectionEntity
import b1a4.harudew.diary.application.port.out.DiaryReflectionRepository
import b1a4.harudew.diary.domain.model.DiaryReflection
import org.springframework.stereotype.Repository

@Repository
class DiaryReflectionRepositoryImpl(
    private val diaryReflectionJpaRepository: DiaryReflectionJpaRepository
) : DiaryReflectionRepository {

    override fun save(diaryReflection: DiaryReflection): DiaryReflection {
        val entity = DiaryReflectionEntity.fromDomain(diaryReflection)
        return diaryReflectionJpaRepository.save(entity).toDomain()
    }
}
