package b1a4.harudew.auth.dto

/**
 * 로그인 성공 시 토큰 응답 DTO
 *
 * 프론트엔드에서 이 토큰을 저장하고 Authorization 헤더에 포함하여 API를 호출합니다.
 *
 * 사용법 (프론트엔드):
 * 1. accessToken을 localStorage 또는 메모리에 저장
 * 2. API 요청 시 Authorization: Bearer {accessToken} 헤더 추가
 * 3. accessToken 만료 시 refreshToken으로 갱신 요청
 *
 * 보안 권장사항:
 * - accessToken은 짧은 만료 시간 (1시간 권장)
 * - refreshToken은 HttpOnly 쿠키로 전송 권장 (현재는 응답 바디에 포함)
 */
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long // accessToken 만료 시간 (초)
)

/**
 * Access Token 갱신 응답 DTO
 */
data class RefreshTokenResponse(
    val accessToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long
)

/**
 * 토큰 갱신 요청 DTO
 */
data class RefreshTokenRequest(
    val refreshToken: String
)
