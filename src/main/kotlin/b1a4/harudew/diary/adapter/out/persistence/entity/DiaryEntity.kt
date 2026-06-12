package b1a4.harudew.diary.adapter.out.persistence.entity

import b1a4.harudew.diary.domain.model.Diary
import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Converter
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Converter
class JsonAnyConverter : jakarta.persistence.AttributeConverter<Any, String> {

    override fun convertToDatabaseColumn(attribute: Any?): String {
        return objectMapper.writeValueAsString(attribute ?: emptyMap<String, Any>())
    }

    override fun convertToEntityAttribute(dbData: String?): Any {
        if (dbData.isNullOrBlank()) return emptyMap<String, Any>()
        return objectMapper.readValue(dbData, Any::class.java)
    }

    companion object {
        private val objectMapper: ObjectMapper = jacksonObjectMapper()
    }
}

@Entity
@Table(name = "diary")
class DiaryEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    val author: MemberEntity,

    @Column(name = "written_date", nullable = false)
    val writtenDate: LocalDate,

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    val content: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "weather", nullable = false)
    val weather: String,

    @Column(name = "photo_path", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    val photoPath: List<String>,

    @Column(name = "audio_path", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter::class)
    val audioPath: List<String>,

    @Column(name = "is_bookmark", nullable = false)
    val isBookmark: Boolean,

    @Column(name = "latitude", nullable = true)
    val latitude: Double?,

    @Column(name = "longitude", nullable = true)
    val longitude: Double?,

    @Lob
    @Column(name = "metadata", nullable = false)
    @Convert(converter = JsonAnyConverter::class)
    val metaData: Any

) {

    fun toDomain() = Diary(
        id = this.id,
        author = this.author.toDomain(),
        writtenDate = this.writtenDate,
        content = this.content,
        title = this.title,
        weather = this.weather,
        photoPath = this.photoPath,
        audioPath = this.audioPath,
        isBookmark = this.isBookmark,
        latitude = this.latitude,
        longitude = this.longitude,
        metaData = this.metaData
    )

    companion object {
        fun fromDomain(domain: Diary, author: MemberEntity) = DiaryEntity(
            id = domain.id,
            author = author,
            writtenDate = domain.writtenDate,
            content = domain.content,
            title = domain.title,
            weather = domain.weather,
            photoPath = domain.photoPath,
            audioPath = domain.audioPath,
            isBookmark = domain.isBookmark,
            latitude = domain.latitude,
            longitude = domain.longitude,
            metaData = domain.metaData
        )
    }
}
