package b1a4.harudew.diary.application.port.out

import b1a4.harudew.diary.domain.model.Diary
import java.time.LocalDate

interface DiaryRepository {

    fun save(diary: Diary) : Diary

    fun findByIdAndAuthorId(diaryId: Long, authorId: String): Diary

    fun findByAuthorId(authorId: String, cursor: Long?, limit: Int): List<Diary>

    fun countByAuthorId(authorId: String): Long

    fun findByAuthorIdAndWrittenDate(authorId: String, date: LocalDate): List<Diary>

    fun searchByAuthorId(authorId: String, query: String): List<Diary>

    fun findBookmarksByAuthorId(authorId: String, page: Int, limit: Int): List<Diary>

    fun findPhotosByAuthorId(authorId: String, cursor: Long?, limit: Int): List<Diary>

    fun findWrittenDays(authorId: String, year: Int, month: Int): List<Int>

    fun findMapDiaries(authorId: String): List<Diary>

    fun deleteByIdAndAuthorId(diaryId: Long, authorId: String)

    fun toggleBookmark(diaryId: Long, authorId: String): Diary

}
