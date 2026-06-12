package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.adapter.out.persistence.entity.DiaryEntity
import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.diary.domain.model.Diary
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import org.springframework.stereotype.Repository

@Repository
class DiaryRepositoryImpl(
    private val diaryJpaRepository: DiaryJpaRepository,
    private val memberJpaRepository: MemberJpaRepository
) : DiaryRepository {

    override fun save(diary: Diary): Diary {
        val author = memberJpaRepository.getReferenceById(diary.author.id)
        return diaryJpaRepository.save(DiaryEntity.fromDomain(diary, author)).toDomain()
    }

}
