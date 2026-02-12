package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.adapter.out.persistence.entity.DiaryProblemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryProblemJpaRepository : JpaRepository<DiaryProblemEntity, Long>
