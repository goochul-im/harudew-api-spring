package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.adapter.out.persistence.entity.DiaryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryJpaRepository : JpaRepository<DiaryEntity, Long>
