package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.domain.model.DiaryReflection

interface DiaryReflectionRepository {

    fun save(diaryReflection: DiaryReflection): DiaryReflection
}
