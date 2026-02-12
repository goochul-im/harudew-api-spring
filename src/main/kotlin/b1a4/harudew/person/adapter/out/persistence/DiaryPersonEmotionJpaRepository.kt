package b1a4.harudew.person.adapter.out.persistence

import b1a4.harudew.person.adapter.out.persistence.entity.DiaryPersonEmotionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryPersonEmotionJpaRepository : JpaRepository<DiaryPersonEmotionEntity, Long>
