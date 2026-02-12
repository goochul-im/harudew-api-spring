package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.adapter.out.persistence.entity.DiaryReflectionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DiaryReflectionJpaRepository : JpaRepository<DiaryReflectionEntity, Long>
