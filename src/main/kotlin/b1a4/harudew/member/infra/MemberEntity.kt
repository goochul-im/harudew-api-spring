package b1a4.harudew.member.infra

import b1a4.harudew.member.domain.Member
import b1a4.harudew.member.domain.SocialType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import java.time.LocalDate

@Entity
class MemberEntity(

    @Id
    val id: String,

    @Column
    val email: String,

    @Column
    val nickname: String,

    @Column(name = "daily_limit")
    val dailyLimit: Int = 0,

    @Column(name = "social_type")
    @Enumerated(EnumType.STRING)
    val socialType: SocialType,

    @Column
    val character: String,

    @Column(name = "stress_test_date", nullable = true)
    val lastStressTestDate: LocalDate? = null,

    @Column(name = "anxiety_test_date", nullable = true)
    val lastAnxietyTestDate: LocalDate? = null,

    @Column(name = "depression_test_date", nullable = true)
    val lastDepressionTestDate: LocalDate? = null

) {

    fun toDomain() = Member(
        this.id,
        this.email,
        this.nickname,
        this.socialType,
        this.character,
        this.lastStressTestDate,
        this.lastAnxietyTestDate,
        this.lastDepressionTestDate
    )

    companion object {
        fun fromDomain(member: Member) = MemberEntity(
            member.id,
            member.email,
            member.nickname,
            socialType = member.socialType,
            character = member.character,
            lastStressTestDate = member.lastStressTestDate,
            lastAnxietyTestDate = member.lastAnxietyTestDate,
            lastDepressionTestDate = member.lastDepressionTestDate
        )
    }

}
