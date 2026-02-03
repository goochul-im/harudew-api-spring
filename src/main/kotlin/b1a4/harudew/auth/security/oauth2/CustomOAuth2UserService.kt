package b1a4.harudew.auth.security.oauth2

import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.member.domain.Member
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * OAuth2 사용자 정보 로드 및 자동 회원가입 서비스
 *
 * Spring Security OAuth2 로그인 과정에서 호출되어:
 * 1. 소셜 로그인 제공자로부터 사용자 정보를 가져옴
 * 2. 기존 회원이면 정보 반환
 * 3. 신규 회원이면 자동으로 회원가입 후 정보 반환
 *
 * 확장 포인트:
 * - 회원가입 시 추가 정보 설정: createNewMember() 메서드 수정
 * - 로그인 시 정보 업데이트: updateExistingMember() 메서드 추가
 * - 회원가입 후처리: 이벤트 발행 등
 */
@Service
class CustomOAuth2UserService(
    private val memberRepository: MemberJpaRepository
) : DefaultOAuth2UserService() {

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)
        val registrationId = userRequest.clientRegistration.registrationId

        return processOAuth2User(registrationId, oAuth2User)
    }

    /**
     * OAuth2 사용자 처리 (조회 또는 생성)
     */
    private fun processOAuth2User(registrationId: String, oAuth2User: OAuth2User): OAuth2User {
        val userInfo = OAuth2UserInfoFactory.create(registrationId, oAuth2User.attributes)
        val memberId = userInfo.getId()

        // 기존 회원 조회 또는 신규 회원 생성
        val member = memberRepository.findById(memberId)
            .map { existingEntity ->
                logger.info { "기존 회원 로그인: id=${memberId}, socialType=${userInfo.getSocialType()}" }
                // 확장: 기존 회원 정보 업데이트가 필요하면 여기서 처리
                // updateExistingMember(existingEntity, userInfo)
                existingEntity.toDomain()
            }
            .orElseGet {
                logger.info { "신규 회원 자동 가입: id=${memberId}, socialType=${userInfo.getSocialType()}" }
                createNewMember(userInfo)
            }

        return CustomOAuth2User(member, oAuth2User.attributes)
    }

    /**
     * 신규 회원 생성
     *
     * 확장 포인트:
     * - 기본 캐릭터 설정 로직 추가
     * - 기본 일일 제한 설정
     * - 회원가입 이벤트 발행
     */
    private fun createNewMember(userInfo: OAuth2UserInfo): Member {
        val member = Member(
            id = userInfo.getId(),
            email = userInfo.getEmail(),
            nickname = userInfo.getNickname(),
            socialType = userInfo.getSocialType(),
            character = getDefaultCharacter(), // 확장: 랜덤 캐릭터 등
            lastStressTestDate = null,
            lastAnxietyTestDate = null,
            lastDepressionTestDate = null
        )

        val savedEntity = memberRepository.save(MemberEntity.fromDomain(member))

        // 확장: 회원가입 후처리 이벤트 발행
        // eventPublisher.publishEvent(MemberCreatedEvent(savedEntity.toDomain()))

        return savedEntity.toDomain()
    }

    /**
     * 기본 캐릭터 반환
     *
     * 확장: 랜덤 캐릭터 선택 또는 기본값 설정 로직
     */
    private fun getDefaultCharacter(): String {
        // 확장: 랜덤 캐릭터 선택 로직
        // return listOf("character1", "character2", "character3").random()
        return "default"
    }

    /**
     * 기존 회원 정보 업데이트 (선택적)
     *
     * 확장: 소셜 로그인 시마다 닉네임, 이메일 등을 동기화하려면 이 메서드 구현
     */
    // private fun updateExistingMember(entity: MemberEntity, userInfo: OAuth2UserInfo): MemberEntity {
    //     // 예: 닉네임 업데이트
    //     // if (entity.nickname != userInfo.getNickname()) {
    //     //     return memberRepository.save(entity.copy(nickname = userInfo.getNickname()))
    //     // }
    //     return entity
    // }
}

/**
 * 커스텀 OAuth2User 구현체
 *
 * Spring Security의 OAuth2User 인터페이스를 구현하며,
 * 우리 도메인의 Member 정보를 함께 보관합니다.
 */
class CustomOAuth2User(
    val member: Member,
    private val attributes: Map<String, Any>
) : OAuth2User {

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities() = listOf(
        org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")
        // 확장: 관리자 권한 등 추가
        // if (member.isAdmin) SimpleGrantedAuthority("ROLE_ADMIN") else null
    ).filterNotNull()

    override fun getName(): String = member.id
}
