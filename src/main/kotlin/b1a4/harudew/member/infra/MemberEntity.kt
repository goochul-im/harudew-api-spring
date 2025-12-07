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

    @Column
    @Enumerated(EnumType.STRING)
    val socialType: SocialType,

    @Column
    val character: String,

    @Column(name = "last_stress_test_date")
    val lastStressTestDate: LocalDate,

    @Column(name = "last_anxiety_test_date")
    val lastAnxietyTestDate: LocalDate,

    @Column(name = "last_depression_test_date")
    val lastDepressionTestDate: LocalDate

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
            member.socialType,
            member.character,
            member.lastStressTestDate,
            member.lastAnxietyTestDate,
            member.lastDepressionTestDate
        )
    }

}
