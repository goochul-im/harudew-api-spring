package b1a4.harudew.auth.adapter.`in`.web

import b1a4.harudew.auth.annotation.MemberId
import b1a4.harudew.auth.dto.RefreshTokenRequest
import b1a4.harudew.auth.dto.RefreshTokenResponse
import b1a4.harudew.auth.dto.TokenResponse
import b1a4.harudew.auth.security.jwt.JwtTokenProvider
import b1a4.harudew.global.exception.BusinessException
import b1a4.harudew.global.exception.ErrorCode
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

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
@RequestMapping("/api/auth")
class AuthController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val memberRepository: MemberJpaRepository,

    @Value("\${jwt.access-expiration:3600000}")
    private val accessTokenExpiration: Long
) {

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
    fun refreshToken(@RequestBody request: RefreshTokenRequest): ResponseEntity<RefreshTokenResponse> {
        val refreshToken = request.refreshToken

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

        logger.info { "토큰 갱신: memberId=$memberId" }

        return ResponseEntity.ok(
            RefreshTokenResponse(
                accessToken = newAccessToken,
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
    fun logout(@MemberId memberId: String): ResponseEntity<Map<String, String>> {
        logger.info { "로그아웃: memberId=$memberId" }

        // 확장: Refresh Token 삭제
        // refreshTokenRepository.deleteByMemberId(memberId)

        // 확장: Access Token 블랙리스트에 추가
        // tokenBlacklistRepository.add(currentAccessToken)

        return ResponseEntity.ok(mapOf("message" to "로그아웃되었습니다."))
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
    @Profile("dev", "local")
    @GetMapping("/demo")
    fun demoLogin(@RequestParam id: String): ResponseEntity<TokenResponse> {
        val member = memberRepository.findById(id)
            .orElseThrow { BusinessException(ErrorCode.MEMBER_NOT_FOUND) }

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
}
