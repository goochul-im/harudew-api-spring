package b1a4.harudew.member.domain

import java.time.LocalDate

class Member(
    val id: String,
    val email: String,
    val nickname: String,
    val socialType: SocialType,
    val character: String,
    val lastStressTestDate: LocalDate,
    val lastAnxietyTestDate: LocalDate,
    val lastDepressionTestDate: LocalDate
)

enum class SocialType {
    GOOGLE, KAKAO
}
