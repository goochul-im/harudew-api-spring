package b1a4.harudew.person.adapter.out.persistence.entity

import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.person.domain.DiaryPersonEmotion
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "diary_person_emotion")
class DiaryPersonEmotionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "diary_id", nullable = false)
    val diaryId: Long,

    @Column(name = "person_name", nullable = false)
    val personName: String, // TODO: Person 엔티티 구현 후 FK로 전환

    @Column(name = "emotion", nullable = false)
    @Enumerated(EnumType.STRING)
    val emotion: EmotionType,

    @Column(name = "intensity", nullable = false)
    val intensity: Int,

    @Column(name = "name_intimacy", nullable = false)
    val nameIntimacy: Float

) {

    fun toDomain() = DiaryPersonEmotion(
        id = this.id,
        diaryId = this.diaryId,
        personName = this.personName,
        emotion = this.emotion,
        intensity = this.intensity,
        nameIntimacy = this.nameIntimacy
    )

    companion object {
        fun fromDomain(domain: DiaryPersonEmotion) = DiaryPersonEmotionEntity(
            id = domain.id,
            diaryId = domain.diaryId,
            personName = domain.personName,
            emotion = domain.emotion,
            intensity = domain.intensity,
            nameIntimacy = domain.nameIntimacy
        )
    }
}
