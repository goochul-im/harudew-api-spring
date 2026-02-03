package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.domain.model.Diary

interface DiaryRepository {

    fun save(diary: Diary) : Diary

}
