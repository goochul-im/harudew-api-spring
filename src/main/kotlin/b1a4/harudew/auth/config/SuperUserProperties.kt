package b1a4.harudew.auth.config

import b1a4.harudew.member.domain.SocialType
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 테스트용 슈퍼유저 설정
 *
 * local 또는 test 프로파일에서만 활성화됩니다.
 * E2E 테스트 시 소셜 로그인 없이 바로 인증된 상태로 API를 호출할 수 있습니다.
 *
 * 사용법:
 * 1. application-local.yml 또는 application.yml (test)에 설정 추가
 * 2. GET /api/auth/super-token 호출하여 토큰 발급
 * 3. Authorization: Bearer {token} 헤더로 API 호출
 *
 * 설정 예시:
 * super-user:
 *   enabled: true
 *   id: "super-user-id"
 *   nickname: "슈퍼유저"
 *   email: "super@test.com"
 *   social-type: GOOGLE
 */
@ConfigurationProperties(prefix = "super-user")
data class SuperUserProperties(
    /** 슈퍼유저 기능 활성화 여부 */
    val enabled: Boolean = false,

    /** 슈퍼유저 ID */
    val id: String = "super-user-id",

    /** 슈퍼유저 닉네임 */
    val nickname: String = "슈퍼유저",

    /** 슈퍼유저 이메일 */
    val email: String = "super@test.com",

    /** 슈퍼유저 소셜 타입 */
    val socialType: SocialType = SocialType.GOOGLE,

    /**
     * 고정 액세스 토큰 (Postman 등에서 편리하게 사용)
     *
     * 이 값을 Authorization: Bearer {fixedAccessToken} 으로 보내면
     * JWT 검증 없이 슈퍼유저로 인증됩니다.
     */
    val fixedAccessToken: String = "super-access-token-for-e2e-test"
)
