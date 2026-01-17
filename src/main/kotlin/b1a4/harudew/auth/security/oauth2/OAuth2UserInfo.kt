package b1a4.harudew.auth.security.oauth2

import b1a4.harudew.member.domain.SocialType

/**
 * OAuth2 사용자 정보 추상 클래스
 *
 * 각 소셜 로그인 제공자(Google, Kakao 등)에서 받아온 사용자 정보를
 * 통일된 형식으로 변환하기 위한 추상 클래스입니다.
 *
 * 확장 포인트:
 * - 새로운 소셜 로그인 추가 시 이 클래스를 상속하여 구현
 * - 예: NaverOAuth2UserInfo, AppleOAuth2UserInfo 등
 */
abstract class OAuth2UserInfo(
    protected val attributes: Map<String, Any>
) {
    /**
     * 소셜 로그인 제공자의 고유 사용자 ID
     * MemberEntity의 PK로 사용됩니다.
     */
    abstract fun getId(): String

    /**
     * 사용자 닉네임 (표시 이름)
     */
    abstract fun getNickname(): String

    /**
     * 사용자 이메일
     */
    abstract fun getEmail(): String

    /**
     * 소셜 로그인 타입
     */
    abstract fun getSocialType(): SocialType

    /**
     * 프로필 이미지 URL (선택)
     * 확장: 프로필 이미지가 필요하면 오버라이드
     */
    open fun getProfileImageUrl(): String? = null
}

/**
 * Google OAuth2 사용자 정보
 *
 * Google에서 제공하는 사용자 정보:
 * - sub: 고유 ID
 * - email: 이메일
 * - name: 이름
 * - picture: 프로필 이미지 URL
 */
class GoogleOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {

    override fun getId(): String {
        return attributes["sub"] as String
    }

    override fun getNickname(): String {
        return attributes["name"] as? String ?: "Google User"
    }

    override fun getEmail(): String {
        return attributes["email"] as? String ?: ""
    }

    override fun getSocialType(): SocialType {
        return SocialType.GOOGLE
    }

    override fun getProfileImageUrl(): String? {
        return attributes["picture"] as? String
    }
}

/**
 * Kakao OAuth2 사용자 정보
 *
 * Kakao에서 제공하는 사용자 정보 구조:
 * - id: 고유 ID (Long → String 변환)
 * - properties.nickname: 닉네임
 * - kakao_account.email: 이메일
 * - kakao_account.profile.profile_image_url: 프로필 이미지
 *
 * 주의: Kakao는 id를 Long 타입으로 제공하므로 String으로 변환합니다.
 */
class KakaoOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {

    override fun getId(): String {
        // Kakao는 id를 Long으로 제공
        return attributes["id"].toString()
    }

    override fun getNickname(): String {
        val properties = attributes["properties"] as? Map<*, *>
        return properties?.get("nickname") as? String ?: "Kakao User"
    }

    override fun getEmail(): String {
        val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
        return kakaoAccount?.get("email") as? String ?: ""
    }

    override fun getSocialType(): SocialType {
        return SocialType.KAKAO
    }

    override fun getProfileImageUrl(): String? {
        val kakaoAccount = attributes["kakao_account"] as? Map<*, *>
        val profile = kakaoAccount?.get("profile") as? Map<*, *>
        return profile?.get("profile_image_url") as? String
    }
}

/**
 * OAuth2 사용자 정보 팩토리
 *
 * 소셜 로그인 제공자에 따라 적절한 OAuth2UserInfo 구현체를 생성합니다.
 *
 * 확장 포인트:
 * - 새로운 소셜 로그인 추가 시 when 분기 추가
 * - 예: "naver" -> NaverOAuth2UserInfo(attributes)
 */
object OAuth2UserInfoFactory {

    /**
     * 소셜 로그인 제공자에 맞는 OAuth2UserInfo 생성
     *
     * @param registrationId 소셜 로그인 제공자 ID (google, kakao 등)
     * @param attributes OAuth2 사용자 속성
     * @return 해당 제공자의 OAuth2UserInfo 구현체
     *
     * 확장: 새로운 소셜 로그인 추가 시 여기에 분기 추가
     */
    fun create(registrationId: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (registrationId.lowercase()) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            // 확장 포인트: 새로운 소셜 로그인 추가
            // "naver" -> NaverOAuth2UserInfo(attributes)
            // "apple" -> AppleOAuth2UserInfo(attributes)
            else -> throw IllegalArgumentException("지원하지 않는 소셜 로그인입니다: $registrationId")
        }
    }
}
