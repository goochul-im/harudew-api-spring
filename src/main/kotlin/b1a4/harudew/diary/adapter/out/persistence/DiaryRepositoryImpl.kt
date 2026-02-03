package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.diary.domain.model.Diary
import org.springframework.stereotype.Repository

@Repository
class DiaryRepositoryImpl() : DiaryRepository {

    override fun save(diary: Diary): Diary {
        TODO("Not yet implemented")
    }

}
