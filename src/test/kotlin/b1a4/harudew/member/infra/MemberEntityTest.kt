package b1a4.harudew.member.infra

import b1a4.harudew.member.domain.Member
import b1a4.harudew.member.domain.SocialType
import org.assertj.core.api.Assertions.*
import java.time.LocalDate
import kotlin.test.Test

class MemberEntityTest {

    @Test
    fun `엔티티에서 도메인 모델로 변환할 수 있다`(){
        //given
        val entity = MemberEntity(
            "testId",
            "testEmail",
            "testNickname",
            0,
            SocialType.GOOGLE,
            "testCharacter",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 1),
        )

        //when
        val result = entity.toDomain()

        //then
        assertThat(result.id).isEqualTo("testId")
        assertThat(result.email).isEqualTo("testEmail")
        assertThat(result.nickname).isEqualTo("testNickname")
        assertThat(result.socialType).isEqualTo(SocialType.GOOGLE)
        assertThat(result.character).isEqualTo("testCharacter")
        assertThat(result.lastStressTestDate).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(result.lastAnxietyTestDate).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(result.lastDepressionTestDate).isEqualTo(LocalDate.of(2025, 1, 1))
    }

    @Test
    fun `도메인 모델에서 엔티티로 변환할 수 있다`() {
        //given
        val member = Member(
            "testId",
            "testEmail",
            "testNickname",
            SocialType.GOOGLE,
            "testCharacter",
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 1)
        )

        //when
        val result = MemberEntity.fromDomain(member)

        //then
        assertThat(result.id).isEqualTo("testId")
        assertThat(result.email).isEqualTo("testEmail")
        assertThat(result.nickname).isEqualTo("testNickname")
        assertThat(result.socialType).isEqualTo(SocialType.GOOGLE)
        assertThat(result.dailyLimit).isEqualTo(0)
        assertThat(result.character).isEqualTo("testCharacter")
        assertThat(result.lastStressTestDate).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(result.lastAnxietyTestDate).isEqualTo(LocalDate.of(2025, 1, 1))
        assertThat(result.lastDepressionTestDate).isEqualTo(LocalDate.of(2025, 1, 1))
    }
}
