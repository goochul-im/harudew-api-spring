package b1a4.harudew.diary.domain.model

import b1a4.harudew.member.domain.Member
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @param id
 * @param author
 * @param createAt : 이 일기가 생성된 실제 날짜
 * @param writtenDate : 작성자가 이 일기를 작성하려고 지정한 날짜
 * @param content
 * @param title
 * @param weather
 * @param photoPath : 일기에 포함된 사진 경로
 * @param audioPath : 일기에 포함된 녹음 경로
 * @param isBookmark
 * @param latitude : 위도
 * @param longitude : 경도
 * @param metaData : 이 일기가 분석된 json
 */
class Diary(
    val id: Long? = null,
    val author: Member,
    val writtenDate: LocalDate,
    val content: String,
    val title: String = "diary title",
    val weather: String = "NONE",
    val photoPath: List<String>,
    val audioPath: List<String>,
    val isBookmark: Boolean = false,
    val latitude: Double?,
    val longitude: Double?,
    val metaData: Any
)
