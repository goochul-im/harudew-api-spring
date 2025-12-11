package b1a4.harudew.recommend.domain

import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.member.domain.Member
import java.time.LocalDate

/**
 * 감정에 따른 추천 영상 도메인
 * @param id
 * @param videoId 유튜브 영상 id
 * @param title 영상 제목
 * @param emotion 관련 감정
 * @param keyword 영상 관련 키워드
 */
class YoutubeVideo(
    val id: Long,
    val videoId: String,
    val title: String,
    val emotion: EmotionType,
    val keyword: String,
) {
}
