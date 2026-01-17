package b1a4.harudew.auth.security.jwt

import b1a4.harudew.member.domain.SocialType
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

/**
 * JWT 토큰 생성 및 검증을 담당하는 Provider
 *
 * 사용법:
 * 1. 토큰 생성: jwtTokenProvider.generateAccessToken(memberId, socialType, nickname)
 * 2. 토큰 검증: jwtTokenProvider.validateToken(token)
 * 3. 토큰에서 memberId 추출: jwtTokenProvider.getMemberIdFromToken(token)
 *
 * 확장 포인트:
 * - 새로운 클레임 추가: generateAccessToken()의 claims에 추가
 * - 토큰 만료 시간 변경: application.yml의 jwt.access-expiration 수정
 * - Refresh Token 추가: generateRefreshToken() 메서드 구현 (Redis 저장 권장)
 */
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}")
    private val secretKey: String,

    @Value("\${jwt.access-expiration:3600000}") // 기본값: 1시간 (밀리초)
    private val accessTokenExpiration: Long,

    @Value("\${jwt.refresh-expiration:86400000}") // 기본값: 24시간 (밀리초)
    private val refreshTokenExpiration: Long
) {
    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secretKey.toByteArray())
    }

    /**
     * Access Token 생성
     *
     * @param memberId 회원 고유 ID (소셜 로그인 provider의 ID)
     * @param socialType 소셜 로그인 타입 (GOOGLE, KAKAO)
     * @param nickname 회원 닉네임
     * @return 생성된 JWT Access Token
     *
     * 확장: 추가 클레임이 필요하면 claims에 put하세요
     */
    fun generateAccessToken(memberId: String, socialType: SocialType, nickname: String): String {
        val now = Date()
        val expiration = Date(now.time + accessTokenExpiration)

        val claims = mutableMapOf<String, Any>(
            "socialType" to socialType.name,
            "nickname" to nickname
            // 확장 포인트: 추가 클레임을 여기에 넣으세요
            // 예: "roles" to listOf("USER", "ADMIN")
        )

        return Jwts.builder()
            .subject(memberId)
            .claims(claims)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    /**
     * Refresh Token 생성
     *
     * 주의: Refresh Token은 Redis 등 외부 저장소에 저장하여 관리하는 것을 권장합니다.
     * 현재 구현은 기본적인 토큰 생성만 제공합니다.
     *
     * @param memberId 회원 고유 ID
     * @return 생성된 JWT Refresh Token
     */
    fun generateRefreshToken(memberId: String): String {
        val now = Date()
        val expiration = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .subject(memberId)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    /**
     * 토큰 유효성 검증
     *
     * @param token 검증할 JWT 토큰
     * @return 유효한 토큰이면 true, 아니면 false
     *
     * 확장: 추가 검증 로직이 필요하면 이 메서드를 수정하세요
     * 예: 블랙리스트 체크, 토큰 버전 확인 등
     */
    fun validateToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            !claims.expiration.before(Date())
        } catch (e: SignatureException) {
            false // 서명 불일치
        } catch (e: MalformedJwtException) {
            false // 잘못된 형식
        } catch (e: ExpiredJwtException) {
            false // 만료됨
        } catch (e: UnsupportedJwtException) {
            false // 지원하지 않는 형식
        } catch (e: IllegalArgumentException) {
            false // 빈 토큰
        }
    }

    /**
     * 토큰에서 회원 ID 추출
     *
     * @param token JWT 토큰
     * @return 회원 ID (subject)
     */
    fun getMemberIdFromToken(token: String): String {
        return parseClaims(token).subject
    }

    /**
     * 토큰에서 소셜 타입 추출
     *
     * @param token JWT 토큰
     * @return SocialType enum
     */
    fun getSocialTypeFromToken(token: String): SocialType {
        val socialType = parseClaims(token)["socialType"] as String
        return SocialType.valueOf(socialType)
    }

    /**
     * 토큰에서 닉네임 추출
     *
     * @param token JWT 토큰
     * @return 닉네임
     */
    fun getNicknameFromToken(token: String): String {
        return parseClaims(token)["nickname"] as String
    }

    /**
     * 토큰이 Refresh Token인지 확인
     *
     * @param token JWT 토큰
     * @return Refresh Token이면 true
     */
    fun isRefreshToken(token: String): Boolean {
        return try {
            val claims = parseClaims(token)
            claims["type"] == "refresh"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 토큰 Claims 파싱
     * 내부 사용 메서드
     */
    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
