package b1a4.harudew.emotion.adapter.out.persistence.entity

import b1a4.harudew.emotion.domain.DiaryActivityEmotion
import b1a4.harudew.emotion.domain.EmotionBase
import b1a4.harudew.emotion.domain.EmotionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "diary_activity_emotion")
class DiaryActivityEmotionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "diary_id", nullable = false)
    val diaryId: Long,

    @Column(name = "activity_name", nullable = false)
    val activityName: String, // TODO: Activity 엔티티 구현 후 FK로 전환

    @Column(name = "emotion", nullable = false)
    @Enumerated(EnumType.STRING)
    val emotion: EmotionType,

    @Column(name = "emotion_base", nullable = false)
    @Enumerated(EnumType.STRING)
    val emotionBase: EmotionBase,

    @Column(name = "intensity", nullable = false)
    val intensity: Int

) {

    fun toDomain() = DiaryActivityEmotion(
        id = this.id,
        diaryId = this.diaryId,
        activityName = this.activityName,
        emotion = this.emotion,
        emotionBase = this.emotionBase,
        intensity = this.intensity
    )

    companion object {
        fun fromDomain(domain: DiaryActivityEmotion) = DiaryActivityEmotionEntity(
            id = domain.id,
            diaryId = domain.diaryId,
            activityName = domain.activityName,
            emotion = domain.emotion,
            emotionBase = domain.emotionBase,
            intensity = domain.intensity
        )
    }
}
