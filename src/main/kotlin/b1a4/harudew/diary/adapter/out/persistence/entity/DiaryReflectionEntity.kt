package b1a4.harudew.diary.adapter.out.persistence.entity

import b1a4.harudew.diary.domain.model.DiaryReflection
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Converter
class StringListConverter : AttributeConverter<List<String>, String> {

    override fun convertToDatabaseColumn(attribute: List<String>?): String {
        return attribute?.joinToString(DELIMITER) ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<String> {
        if (dbData.isNullOrBlank()) return emptyList()
        return dbData.split(DELIMITER)
    }

    companion object {
        private const val DELIMITER = "|||"
    }
}

@Entity
@Table(name = "diary_reflection")
class DiaryReflectionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "diary_id", nullable = false, unique = true)
    val diaryId: Long,

    @Column(name = "shortcomings", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    val shortcomings: List<String>,

    @Column(name = "tomorrow_mindset", columnDefinition = "TEXT", nullable = true)
    val tomorrowMindset: String? = null

) {

    fun toDomain() = DiaryReflection(
        id = this.id,
        diaryId = this.diaryId,
        shortcomings = this.shortcomings,
        tomorrowMindset = this.tomorrowMindset
    )

    companion object {
        fun fromDomain(domain: DiaryReflection) = DiaryReflectionEntity(
            id = domain.id,
            diaryId = domain.diaryId,
            shortcomings = domain.shortcomings,
            tomorrowMindset = domain.tomorrowMindset
        )
    }
}
