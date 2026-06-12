package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.adapter.out.persistence.entity.DiaryEntity
import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.diary.domain.model.Diary
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.YearMonth

@Repository
class DiaryRepositoryImpl(
    private val diaryJpaRepository: DiaryJpaRepository,
    private val memberJpaRepository: MemberJpaRepository
) : DiaryRepository {

    override fun save(diary: Diary): Diary {
        val author = memberJpaRepository.getReferenceById(diary.author.id)
        return diaryJpaRepository.save(DiaryEntity.fromDomain(diary, author)).toDomain()
    }

    override fun findByIdAndAuthorId(diaryId: Long, authorId: String): Diary {
        return findOwnedEntity(diaryId, authorId).toDomain()
    }

    override fun findByAuthorId(authorId: String, cursor: Long?, limit: Int): List<Diary> {
        val pageable = PageRequest.of(0, limit)
        val entities = if (cursor == null || cursor <= 0L) {
            diaryJpaRepository.findRecentByAuthor(authorId, pageable)
        } else {
            diaryJpaRepository.findRecentByAuthorBeforeCursor(authorId, cursor, pageable)
        }
        return entities.map { it.toDomain() }
    }

    override fun countByAuthorId(authorId: String): Long =
        diaryJpaRepository.countByAuthor(authorId)

    override fun findByAuthorIdAndWrittenDate(authorId: String, date: LocalDate): List<Diary> =
        diaryJpaRepository.findByAuthorAndWrittenDate(authorId, date).map { it.toDomain() }

    override fun searchByAuthorId(authorId: String, query: String): List<Diary> =
        diaryJpaRepository.searchByAuthor(authorId, query).map { it.toDomain() }

    override fun findBookmarksByAuthorId(authorId: String, page: Int, limit: Int): List<Diary> =
        diaryJpaRepository.findBookmarksByAuthor(authorId, PageRequest.of(page.coerceAtLeast(0), limit))
            .map { it.toDomain() }

    override fun findPhotosByAuthorId(authorId: String, cursor: Long?, limit: Int): List<Diary> {
        return findByAuthorId(authorId, cursor, limit * 3)
            .filter { it.photoPath.isNotEmpty() }
            .take(limit)
    }

    override fun findWrittenDays(authorId: String, year: Int, month: Int): List<Int> {
        val yearMonth = YearMonth.of(year, month)
        return diaryJpaRepository.findByAuthorAndWrittenDateBetween(
            authorId = authorId,
            start = yearMonth.atDay(1),
            end = yearMonth.atEndOfMonth()
        )
            .map { it.writtenDate.dayOfMonth }
            .distinct()
            .sorted()
    }

    override fun findMapDiaries(authorId: String): List<Diary> =
        diaryJpaRepository.findMapByAuthor(authorId).map { it.toDomain() }

    override fun deleteByIdAndAuthorId(diaryId: Long, authorId: String) {
        diaryJpaRepository.delete(findOwnedEntity(diaryId, authorId))
    }

    override fun toggleBookmark(diaryId: Long, authorId: String): Diary {
        val entity = findOwnedEntity(diaryId, authorId)
        val updated = DiaryEntity.fromDomain(
            entity.toDomain().copyWithBookmark(!entity.isBookmark),
            entity.author
        )
        return diaryJpaRepository.save(updated).toDomain()
    }

    private fun findOwnedEntity(diaryId: Long, authorId: String): DiaryEntity =
        diaryJpaRepository.findOwnedById(diaryId, authorId)
            .orElseThrow { NoSuchElementException("일기를 찾을 수 없습니다. diaryId=$diaryId") }

    private fun Diary.copyWithBookmark(isBookmarked: Boolean): Diary =
        Diary(
            id = id,
            author = author,
            writtenDate = writtenDate,
            content = content,
            title = title,
            weather = weather,
            photoPath = photoPath,
            audioPath = audioPath,
            isBookmark = isBookmarked,
            latitude = latitude,
            longitude = longitude,
            metaData = metaData
        )
}
