package b1a4.harudew.auth.config

import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.member.domain.Member
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

private val logger = KotlinLogging.logger {}

/**
 * 슈퍼유저 설정
 *
 * local 또는 test 프로파일에서만 활성화됩니다.
 * 애플리케이션 시작 시 슈퍼유저가 DB에 존재하지 않으면 자동으로 생성합니다.
 */
@Configuration
@Profile("local", "test")
@EnableConfigurationProperties(SuperUserProperties::class)
class SuperUserConfig {

    @Bean
    fun superUserInitializer(
        superUserProperties: SuperUserProperties,
        memberRepository: MemberJpaRepository
    ): CommandLineRunner = CommandLineRunner {
        if (!superUserProperties.enabled) {
            logger.info { "슈퍼유저 기능이 비활성화되어 있습니다." }
            return@CommandLineRunner
        }

        val existingMember = memberRepository.findById(superUserProperties.id)
        if (existingMember.isPresent) {
            logger.info { "슈퍼유저가 이미 존재합니다: id=${superUserProperties.id}" }
            return@CommandLineRunner
        }

        val superUser = Member(
            id = superUserProperties.id,
            email = superUserProperties.email,
            nickname = superUserProperties.nickname,
            socialType = superUserProperties.socialType,
            character = "super"
        )

        memberRepository.save(MemberEntity.fromDomain(superUser))
        logger.info { "슈퍼유저 생성 완료: id=${superUserProperties.id}, nickname=${superUserProperties.nickname}" }
    }
}
