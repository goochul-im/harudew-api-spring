package b1a4.harudew.auth.adapter.`in`.web

import b1a4.harudew.auth.dto.RefreshTokenRequest
import b1a4.harudew.auth.security.jwt.JwtTokenProvider
import b1a4.harudew.member.adapter.out.infrastructure.MemberEntity
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.member.domain.SocialType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.Optional

class AuthControllerTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var memberRepository: MemberJpaRepository
    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = mock()
        memberRepository = mock()

        mockMvc = MockMvcBuilders
            .standaloneSetup(
                AuthController(
                    jwtTokenProvider = jwtTokenProvider,
                    memberRepository = memberRepository,
                    accessTokenExpiration = 3_600_000
                )
            )
            .build()
    }

    @Test
    @DisplayName("POST /auth/refresh: Authorization 헤더 refresh token으로 snake_case와 camelCase 토큰을 반환한다")
    fun `refresh accepts authorization header on frontend compatible path`() {
        // given
        val member = memberEntity("member-1")
        whenever(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true)
        whenever(jwtTokenProvider.isRefreshToken("refresh-token")).thenReturn(true)
        whenever(jwtTokenProvider.getMemberIdFromToken("refresh-token")).thenReturn(member.id)
        whenever(memberRepository.findById(member.id)).thenReturn(Optional.of(member))
        whenever(jwtTokenProvider.generateAccessToken(member.id, member.socialType, member.nickname))
            .thenReturn("new-access-token")
        whenever(jwtTokenProvider.generateRefreshToken(member.id)).thenReturn("new-refresh-token")

        // when & then
        mockMvc.perform(
            post("/auth/refresh")
                .header(HttpHeaders.AUTHORIZATION, "Bearer refresh-token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("new-access-token"))
            .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
            .andExpect(jsonPath("$.access_token").value("new-access-token"))
            .andExpect(jsonPath("$.refresh_token").value("new-refresh-token"))
            .andExpect(jsonPath("$.expiresIn").value(3600))
            .andExpect(jsonPath("$.expires_in").value(3600))
    }

    @Test
    @DisplayName("POST /api/auth/refresh: body refreshToken 방식도 유지한다")
    fun `refresh keeps api path and body contract`() {
        // given
        val member = memberEntity("member-2")
        whenever(jwtTokenProvider.validateToken("body-refresh-token")).thenReturn(true)
        whenever(jwtTokenProvider.isRefreshToken("body-refresh-token")).thenReturn(true)
        whenever(jwtTokenProvider.getMemberIdFromToken("body-refresh-token")).thenReturn(member.id)
        whenever(memberRepository.findById(member.id)).thenReturn(Optional.of(member))
        whenever(jwtTokenProvider.generateAccessToken(member.id, member.socialType, member.nickname))
            .thenReturn("new-access-token")
        whenever(jwtTokenProvider.generateRefreshToken(member.id)).thenReturn("new-refresh-token")

        // when & then
        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshTokenRequest("body-refresh-token")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.access_token").value("new-access-token"))
            .andExpect(jsonPath("$.refresh_token").value("new-refresh-token"))
    }

    @Test
    @DisplayName("GET /auth/demo: 없는 demo member는 생성하고 프론트 호환 토큰 응답을 반환한다")
    fun `demo login creates missing demo member`() {
        // given
        val savedMember = memberEntity("anne", nickname = "안네")
        whenever(memberRepository.findById("anne")).thenReturn(Optional.empty())
        whenever(memberRepository.save(any())).thenReturn(savedMember)
        whenever(jwtTokenProvider.generateAccessToken(savedMember.id, savedMember.socialType, savedMember.nickname))
            .thenReturn("demo-access-token")
        whenever(jwtTokenProvider.generateRefreshToken(savedMember.id)).thenReturn("demo-refresh-token")

        // when & then
        mockMvc.perform(get("/auth/demo").param("id", "anne"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").value("demo-access-token"))
            .andExpect(jsonPath("$.refreshToken").value("demo-refresh-token"))
            .andExpect(jsonPath("$.access_token").value("demo-access-token"))
            .andExpect(jsonPath("$.refresh_token").value("demo-refresh-token"))

        verify(memberRepository).save(any())
    }

    @Test
    @DisplayName("GET /auth/google: origin/frontend 호환 OAuth2 진입점에서 Spring OAuth2 시작점으로 보낸다")
    fun `google login redirects to spring oauth authorization endpoint`() {
        mockMvc.perform(get("/auth/google").param("state", "http://localhost:5173"))
            .andExpect(status().isFound)
            .andExpect(header().exists(HttpHeaders.LOCATION))
            .andExpect(redirectedUrl("/oauth2/authorization/google?redirect_uri=http://localhost:5173"))
    }

    private fun memberEntity(id: String, nickname: String = "테스트유저"): MemberEntity {
        return MemberEntity(
            id = id,
            email = "$id@test.local",
            nickname = nickname,
            socialType = SocialType.GOOGLE,
            character = "test"
        )
    }
}
