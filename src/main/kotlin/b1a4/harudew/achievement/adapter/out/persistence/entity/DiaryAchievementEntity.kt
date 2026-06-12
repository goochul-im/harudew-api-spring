package b1a4.harudew.achievement.adapter.out.persistence.entity

import b1a4.harudew.achievement.domain.DiaryAchievement
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
class DoubleListConverter : AttributeConverter<List<Double>, String> {

    override fun convertToDatabaseColumn(attribute: List<Double>?): String {
        return attribute?.joinToString(DELIMITER) ?: ""
    }

    override fun convertToEntityAttribute(dbData: String?): List<Double> {
        if (dbData.isNullOrBlank()) return emptyList()
        return dbData.split(DELIMITER).mapNotNull { it.toDoubleOrNull() }
    }

    companion object {
        private const val DELIMITER = "|||"
    }
}

@Entity
@Table(name = "diary_achievement")
class DiaryAchievementEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "diary_id", nullable = false)
    val diaryId: Long,

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    val content: String,

    @Column(name = "vector", columnDefinition = "TEXT", nullable = false)
    @Convert(converter = DoubleListConverter::class)
    val vector: List<Double>,

    @Column(name = "cluster_id", nullable = true)
    val clusterId: Long? = null

) {

    fun toDomain() = DiaryAchievement(
        id = this.id,
        diaryId = this.diaryId,
        content = this.content,
        vector = this.vector,
        clusterId = this.clusterId
    )

    companion object {
        fun fromDomain(domain: DiaryAchievement) = DiaryAchievementEntity(
            id = domain.id,
            diaryId = domain.diaryId,
            content = domain.content,
            vector = domain.vector,
            clusterId = domain.clusterId
        )
    }
}
