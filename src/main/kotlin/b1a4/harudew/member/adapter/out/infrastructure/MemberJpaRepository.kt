package b1a4.harudew.member.adapter.out.infrastructure

import b1a4.harudew.member.domain.SocialType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

/**
 * 회원 JPA Repository
 *
 * 확장 포인트:
 * - 새로운 조회 메서드 추가: findBy... 네이밍 컨벤션 사용
 * - 복잡한 쿼리: @Query 어노테이션 또는 QueryDSL 사용
 */
interface MemberJpaRepository : JpaRepository<MemberEntity, String> {

    /**
     * 소셜 타입과 ID로 회원 조회
     *
     * @param id 소셜 로그인 provider의 고유 ID
     * @param socialType 소셜 로그인 타입 (GOOGLE, KAKAO)
     */
    fun findByIdAndSocialType(id: String, socialType: SocialType): Optional<MemberEntity>

    /**
     * 이메일로 회원 조회
     */
    fun findByEmail(email: String): Optional<MemberEntity>

    /**
     * 이메일 존재 여부 확인
     */
    fun existsByEmail(email: String): Boolean
}
