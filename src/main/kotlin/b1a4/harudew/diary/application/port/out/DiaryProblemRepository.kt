package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.domain.model.DiaryProblem

interface DiaryProblemRepository {

    fun saveAll(diaryProblems: List<DiaryProblem>): List<DiaryProblem>
}
