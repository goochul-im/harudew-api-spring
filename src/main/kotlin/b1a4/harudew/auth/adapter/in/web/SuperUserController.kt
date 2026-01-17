package b1a4.harudew.auth.adapter.`in`.web

import b1a4.harudew.auth.config.SuperUserProperties
import b1a4.harudew.auth.dto.TokenResponse
import b1a4.harudew.auth.security.jwt.JwtTokenProvider
import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

/**
 * E2E 테스트용 슈퍼유저 토큰 발급 컨트롤러
 *
 * local 또는 test 프로파일에서만 활성화됩니다.
 * 소셜 로그인 없이 바로 인증 토큰을 발급받을 수 있습니다.
 *
 * 사용법:
 * GET /api/auth/super-token
 *
 * 응답:
 * {
 *   "accessToken": "...",
 *   "refreshToken": "...",
 *   "expiresIn": 3600
 * }
 */
@RestController
@RequestMapping("/api/auth")
@Profile("local", "test")
class SuperUserController(
    private val superUserProperties: SuperUserProperties,
    private val jwtTokenProvider: JwtTokenProvider,

    @Value("\${jwt.access-expiration:3600000}")
    private val accessTokenExpiration: Long
) {

    /**
     * 슈퍼유저 토큰 발급
     *
     * 슈퍼유저 설정이 활성화되어 있으면 즉시 토큰을 발급합니다.
     * E2E 테스트에서 소셜 로그인 없이 API를 테스트할 때 사용합니다.
     */
    @GetMapping("/super-token")
    fun getSuperToken(): ResponseEntity<TokenResponse> {
        if (!superUserProperties.enabled) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        val accessToken = jwtTokenProvider.generateAccessToken(
            memberId = superUserProperties.id,
            socialType = superUserProperties.socialType,
            nickname = superUserProperties.nickname
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(superUserProperties.id)

        logger.info { "슈퍼유저 토큰 발급: id=${superUserProperties.id}" }

        return ResponseEntity.ok(
            TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = accessTokenExpiration / 1000
            )
        )
    }

    /**
     * 슈퍼유저 정보 조회
     *
     * 현재 설정된 슈퍼유저 정보를 반환합니다.
     */
    @GetMapping("/super-user")
    fun getSuperUserInfo(): ResponseEntity<Map<String, Any>> {
        if (!superUserProperties.enabled) {
            throw BusinessException(ErrorCode.ACCESS_DENIED)
        }

        return ResponseEntity.ok(
            mapOf(
                "enabled" to superUserProperties.enabled,
                "id" to superUserProperties.id,
                "nickname" to superUserProperties.nickname,
                "email" to superUserProperties.email,
                "socialType" to superUserProperties.socialType.name
            )
        )
    }
}
