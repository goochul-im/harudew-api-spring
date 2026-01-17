package b1a4.harudew.auth.security.oauth2

import b1a4.harudew.auth.security.jwt.JwtTokenProvider
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

private val logger = KotlinLogging.logger {}

/**
 * OAuth2 로그인 성공 핸들러
 *
 * 소셜 로그인 성공 시 호출되어:
 * 1. JWT Access Token 생성
 * 2. JWT Refresh Token 생성
 * 3. 프론트엔드로 토큰과 함께 리다이렉트
 *
 * 확장 포인트:
 * - 토큰 전달 방식 변경: 쿠키 vs URL 파라미터
 * - Refresh Token 저장소: Redis 등 외부 저장소 연동
 * - 로그인 성공 이벤트 처리: 로그 기록, 마지막 로그인 시간 업데이트 등
 */
@Component
class OAuth2SuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val cookieAuthorizationRequestRepository: HttpCookieOAuth2AuthorizationRequestRepository,

    @Value("\${oauth2.redirect-uri:http://localhost:3000/getaccess}")
    private val redirectUri: String,

    @Value("\${jwt.access-expiration:3600000}")
    private val accessTokenExpiration: Long
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as CustomOAuth2User
        val member = oAuth2User.member

        logger.info { "OAuth2 로그인 성공: id=${member.id}, socialType=${member.socialType}" }

        // JWT 토큰 생성
        val accessToken = jwtTokenProvider.generateAccessToken(
            memberId = member.id,
            socialType = member.socialType,
            nickname = member.nickname
        )

        val refreshToken = jwtTokenProvider.generateRefreshToken(member.id)

        // 확장: Refresh Token을 Redis 등에 저장
        // refreshTokenRepository.save(RefreshToken(member.id, refreshToken, ttl))

        // 리다이렉트 URL 생성
        val targetUrl = buildRedirectUrl(accessToken, refreshToken)

        // 확장: 토큰을 쿠키로 전송하려면 여기서 쿠키 설정
        // setTokenCookies(response, accessToken, refreshToken)

        // OAuth2 인증 쿠키 정리
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response)
        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    /**
     * 리다이렉트 URL 생성
     *
     * 현재: URL 쿼리 파라미터로 토큰 전달
     *
     * 확장: 보안 강화를 위해 쿠키 방식으로 변경 권장
     * - accessToken: 메모리에 저장 (XSS 취약점 주의)
     * - refreshToken: HttpOnly 쿠키로 전송
     */
    private fun buildRedirectUrl(accessToken: String, refreshToken: String): String {
        return UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("access", accessToken)
            .queryParam("refresh", refreshToken)
            .queryParam("expiresIn", accessTokenExpiration / 1000) // 초 단위
            .build()
            .toUriString()
    }

    /**
     * HttpOnly 쿠키로 토큰 설정 (선택적)
     *
     * 확장: URL 파라미터 대신 쿠키로 토큰 전달 시 사용
     */
    // private fun setTokenCookies(response: HttpServletResponse, accessToken: String, refreshToken: String) {
    //     // Access Token 쿠키 (HttpOnly X - JavaScript에서 접근 필요)
    //     val accessCookie = Cookie("accessToken", accessToken).apply {
    //         path = "/"
    //         maxAge = (accessTokenExpiration / 1000).toInt()
    //         isHttpOnly = false
    //         secure = true // HTTPS에서만
    //     }
    //
    //     // Refresh Token 쿠키 (HttpOnly O - JavaScript 접근 불가)
    //     val refreshCookie = Cookie("refreshToken", refreshToken).apply {
    //         path = "/api/auth/refresh"
    //         maxAge = (refreshTokenExpiration / 1000).toInt()
    //         isHttpOnly = true
    //         secure = true
    //     }
    //
    //     response.addCookie(accessCookie)
    //     response.addCookie(refreshCookie)
    // }
}
