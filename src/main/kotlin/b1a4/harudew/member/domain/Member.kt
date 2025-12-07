package b1a4.harudew.member.domain

import java.time.LocalDate

class Member(
    val id: String,
    val email: String,
    val nickname: String,
    val socialType: SocialType,
    val character: String,
    val lastStressTestDate: LocalDate? = null,
    val lastAnxietyTestDate: LocalDate? = null,
    val lastDepressionTestDate: LocalDate? = null
)

enum class SocialType {
    GOOGLE, KAKAO
}
