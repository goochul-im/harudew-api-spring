package b1a4.harudew.emotion.adapter.out.persistence

import b1a4.harudew.emotion.adapter.out.persistence.entity.DiaryActivityEmotionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryActivityEmotionJpaRepository : JpaRepository<DiaryActivityEmotionEntity, Long>
