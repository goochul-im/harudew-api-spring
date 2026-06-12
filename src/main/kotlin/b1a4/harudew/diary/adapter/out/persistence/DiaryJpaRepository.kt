package b1a4.harudew.diary.adapter.out.persistence

import b1a4.harudew.diary.adapter.out.persistence.entity.DiaryEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

interface DiaryJpaRepository : JpaRepository<DiaryEntity, Long> {

    @Query("select d from DiaryEntity d where d.id = :id and d.author.id = :authorId")
    fun findOwnedById(
        @Param("id") id: Long,
        @Param("authorId") authorId: String
    ): Optional<DiaryEntity>

    @Query("select d from DiaryEntity d where d.author.id = :authorId order by d.id desc")
    fun findRecentByAuthor(
        @Param("authorId") authorId: String,
        pageable: Pageable
    ): List<DiaryEntity>

    @Query("select d from DiaryEntity d where d.author.id = :authorId and d.id < :cursor order by d.id desc")
    fun findRecentByAuthorBeforeCursor(
        @Param("authorId") authorId: String,
        @Param("cursor") cursor: Long,
        pageable: Pageable
    ): List<DiaryEntity>

    @Query("select count(d) from DiaryEntity d where d.author.id = :authorId")
    fun countByAuthor(@Param("authorId") authorId: String): Long

    @Query("select d from DiaryEntity d where d.author.id = :authorId and d.writtenDate = :date order by d.id desc")
    fun findByAuthorAndWrittenDate(
        @Param("authorId") authorId: String,
        @Param("date") date: LocalDate
    ): List<DiaryEntity>

    @Query(
        "select d from DiaryEntity d " +
            "where d.author.id = :authorId and lower(d.content) like lower(concat('%', :query, '%')) " +
            "order by d.writtenDate desc, d.id desc"
    )
    fun searchByAuthor(
        @Param("authorId") authorId: String,
        @Param("query") query: String
    ): List<DiaryEntity>

    @Query("select d from DiaryEntity d where d.author.id = :authorId and d.isBookmark = true order by d.id desc")
    fun findBookmarksByAuthor(
        @Param("authorId") authorId: String,
        pageable: Pageable
    ): List<DiaryEntity>

    @Query("select d from DiaryEntity d where d.author.id = :authorId and d.writtenDate between :start and :end")
    fun findByAuthorAndWrittenDateBetween(
        @Param("authorId") authorId: String,
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<DiaryEntity>

    @Query(
        "select d from DiaryEntity d " +
            "where d.author.id = :authorId and d.latitude is not null and d.longitude is not null " +
            "order by d.id desc"
    )
    fun findMapByAuthor(@Param("authorId") authorId: String): List<DiaryEntity>
}
