package b1a4.harudew.auth.adapter.`in`.web

import b1a4.harudew.auth.annotation.MemberId
import b1a4.harudew.auth.dto.RefreshTokenRequest
import b1a4.harudew.auth.dto.RefreshTokenResponse
import b1a4.harudew.auth.dto.TokenResponse
import b1a4.harudew.auth.security.jwt.JwtTokenProvider
import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode
import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.member.domain.Member
import b1a4.harudew.member.domain.SocialType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder

private val logger = KotlinLogging.logger {}

/**
 * 인증 관련 API 컨트롤러
 *
 * 제공하는 기능:
 * - OAuth2 로그인 진입점 (Google, Kakao)
 * - Access Token 갱신
 * - 로그아웃
 * - 토큰 유효성 검증
 *
 * 확장 포인트:
 * - 새로운 인증 방식 추가: 새 엔드포인트 생성
 * - 로그아웃 처리 강화: Redis에서 Refresh Token 삭제
 * - 회원 탈퇴: deleteAccount 엔드포인트 추가
 *
 * OAuth2 로그인 흐름:
 * 1. 프론트엔드에서 /oauth2/authorization/google 또는 /oauth2/authorization/kakao로 리다이렉트
 * 2. Spring Security가 OAuth2 제공자로 리다이렉트
 * 3. 사용자 인증 후 콜백 처리
 * 4. OAuth2SuccessHandler에서 JWT 발급 및 프론트엔드로 리다이렉트
 */
@RestController
@RequestMapping(value = ["/api/auth", "/auth"])
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberRepository: MemberJpaRepository,

    @Value("\${jwt.access-expiration:3600000}")
    private val accessTokenExpiration: Long
) {

    /**
     * 프론트 호환 OAuth2 로그인 진입점.
     *
     * Spring Security 기본 진입점은 /oauth2/authorization/{provider} 이지만,
     * origin-backend와 frontend는 /auth/google, /auth/kakao를 호출한다.
     */
    @GetMapping("/google")
    fun googleLogin(
        @RequestParam("state", required = false) state: String?,
        @RequestParam("redirect_uri", required = false) redirectUri: String?
    ): RedirectView {
        return RedirectView(buildAuthorizationUri("google", state, redirectUri))
    }

    @GetMapping("/kakao")
    fun kakaoLogin(
        @RequestParam("state", required = false) state: String?,
        @RequestParam("redirect_uri", required = false) redirectUri: String?
    ): RedirectView {
        return RedirectView(buildAuthorizationUri("kakao", state, redirectUri))
    }

    /**
     * Access Token 갱신
     *
     * Refresh Token을 사용하여 새로운 Access Token을 발급합니다.
     *
     * 요청:
     * POST /api/auth/refresh
     * Body: { "refreshToken": "..." }
     *
     * 응답:
     * { "accessToken": "...", "tokenType": "Bearer", "expiresIn": 3600 }
     *
     * 확장: Refresh Token Rotation 구현 권장
     * - 새 Refresh Token도 함께 발급
     * - 기존 Refresh Token은 무효화
     */
    @PostMapping("/refresh")
    fun refreshToken(
        @RequestBody(required = false) request: RefreshTokenRequest?,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?
    ): ResponseEntity<RefreshTokenResponse> {
        val refreshToken = extractBearerToken(authorization)
            ?: request?.refreshToken
            ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        // Refresh Token 타입 확인
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }

        val memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken)

        // 회원 정보 조회
        val member = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        // 확장: Redis에서 저장된 Refresh Token과 비교
        // val storedToken = refreshTokenRepository.findByMemberId(memberId)
        // if (storedToken != refreshToken) throw BusinessException(ErrorCode.INVALID_TOKEN)

        // 새 Access Token 발급
        val newAccessToken = jwtTokenProvider.generateAccessToken(
            memberId = member.id,
            socialType = member.socialType,
            nickname = member.nickname
        )
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(member.id)

        logger.info { "토큰 갱신: memberId=$memberId" }

        return ResponseEntity.ok(
            RefreshTokenResponse(
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
                expiresIn = accessTokenExpiration / 1000
            )
        )
    }

    /**
     * 로그아웃
     *
     * 현재 구현은 클라이언트 측에서 토큰 삭제로 처리됩니다.
     * 보안 강화를 위해 서버 측 Refresh Token 무효화를 권장합니다.
     *
     * 요청: POST /api/auth/logout (Authorization 헤더 필요)
     *
     * 확장:
     * - Redis에서 Refresh Token 삭제
     * - 토큰 블랙리스트 추가
     */
    @PostMapping("/logout")
    fun logout(@MemberId memberId: String): ResponseEntity<Map<String, Any>> {
        logger.info { "로그아웃: memberId=$memberId" }

        // 확장: Refresh Token 삭제
        // refreshTokenRepository.deleteByMemberId(memberId)

        // 확장: Access Token 블랙리스트에 추가
        // tokenBlacklistRepository.add(currentAccessToken)

        return ResponseEntity.ok(
            mapOf(
                "success" to true,
                "message" to "로그아웃되었습니다."
            )
        )
    }

    /**
     * 토큰 유효성 테스트
     *
     * 현재 Access Token이 유효한지 확인합니다.
     * 프론트엔드에서 앱 시작 시 토큰 유효성 확인용으로 사용합니다.
     *
     * 요청: GET /api/auth/test (Authorization 헤더 필요)
     * 응답: 200 OK + 회원 정보
     */
    @GetMapping("/test")
    fun testToken(@MemberId memberId: String): ResponseEntity<Map<String, Any>> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

        return ResponseEntity.ok(
            mapOf(
                "valid" to true,
                "memberId" to member.id,
                "nickname" to member.nickname,
                "socialType" to member.socialType.name
            )
        )
    }

    /**
     * 데모 로그인 (개발용)
     *
     * 테스트 목적으로 특정 회원으로 바로 로그인합니다.
     * 프로덕션에서는 비활성화하거나 제거해야 합니다.
     *
     * 확장: @Profile("dev") 추가하여 개발 환경에서만 활성화
     */
    @GetMapping("/demo")
    fun demoLogin(@RequestParam id: String): ResponseEntity<TokenResponse> {
        val member = findOrCreateDemoMember(id)

        val accessToken = jwtTokenProvider.generateAccessToken(
            memberId = member.id,
            socialType = member.socialType,
            nickname = member.nickname
        )
        val refreshToken = jwtTokenProvider.generateRefreshToken(member.id)

        return ResponseEntity.ok(
            TokenResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = accessTokenExpiration / 1000
            )
        )
    }

    private fun buildAuthorizationUri(provider: String, state: String?, redirectUri: String?): String {
        val frontendRedirectUri = redirectUri ?: state

        val builder = UriComponentsBuilder
            .fromPath("/oauth2/authorization/{provider}")

        if (!frontendRedirectUri.isNullOrBlank()) {
            builder.queryParam("redirect_uri", frontendRedirectUri)
        }

        return builder
            .buildAndExpand(provider)
            .toUriString()
    }

    private fun extractBearerToken(authorization: String?): String? {
        return authorization
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.substring(BEARER_PREFIX.length)
    }

    private fun findOrCreateDemoMember(id: String): MemberEntity {
        return memberRepository.findById(id)
            .orElseGet {
                val member = Member(
                    id = id,
                    email = "$id@demo.harudew.local",
                    nickname = demoNickname(id),
                    socialType = SocialType.GOOGLE,
                    character = "demo"
                )
                memberRepository.save(MemberEntity.fromDomain(member))
            }
    }

    private fun demoNickname(id: String): String {
        return when (id) {
            "traveler" -> "여행자"
            "lee" -> "이순신"
            "harry" -> "해리"
            "namul" -> "나물이"
            "anne" -> "안네"
            "demo" -> "데모"
            else -> id
        }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
