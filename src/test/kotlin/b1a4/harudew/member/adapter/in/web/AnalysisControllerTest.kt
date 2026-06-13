package b1a4.harudew.member.adapter.`in`.web

import b1a4.harudew.auth.annotation.CurrentMember
import b1a4.harudew.emotion.adapter.dto.response.GraphEmotionResponse
import b1a4.harudew.emotion.adapter.dto.response.GraphResponse
import b1a4.harudew.emotion.adapter.dto.response.NegativeEmotionByTargetResponse
import b1a4.harudew.emotion.adapter.dto.response.WillBeDeprecatedDTO
import b1a4.harudew.emotion.adapter.`in`.web.EmotionController
import b1a4.harudew.emotion.adapter.`in`.web.RelationController
import b1a4.harudew.emotion.domain.EmotionGroup
import b1a4.harudew.member.adapter.dto.response.EmotionBaseAnalysis
import b1a4.harudew.member.adapter.dto.response.EmotionBaseAnalysisRes
import b1a4.harudew.member.adapter.dto.response.StrengthResponse
import b1a4.harudew.member.adapter.out.infrastructure.MemberJpaRepository
import b1a4.harudew.member.application.service.AnalysisAggregationService
import b1a4.harudew.member.domain.Member
import b1a4.harudew.member.domain.SocialType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

class AnalysisControllerTest {

    private lateinit var aggregationService: AnalysisAggregationService
    private lateinit var memberJpaRepository: MemberJpaRepository
    private lateinit var mockMvc: MockMvc

    private val member = Member(
        id = "member-1",
        email = "member@test.local",
        nickname = "테스터",
        socialType = SocialType.GOOGLE,
        character = "test"
    )

    @BeforeEach
    fun setUp() {
        aggregationService = mock()
        memberJpaRepository = mock()
        mockMvc = MockMvcBuilders
            .standaloneSetup(
                MemberController(aggregationService, memberJpaRepository),
                StrengthController(aggregationService),
                EmotionController(aggregationService),
                RelationController(aggregationService)
            )
            .setCustomArgumentResolvers(FixedCurrentMemberResolver(member))
            .build()
    }

    @Test
    @DisplayName("GET /member/emotion/base-analysis: 프론트 about-me 감정 배열 계약을 반환한다")
    fun `member base analysis returns frontend contract`() {
        whenever(aggregationService.emotionBaseAnalysis(member.id)).thenReturn(
            EmotionBaseAnalysisRes(
                relation = listOf(EmotionBaseAnalysis("친밀", 8, 1)),
                self = listOf(EmotionBaseAnalysis("성취감", 5, 1)),
                state = listOf(EmotionBaseAnalysis("행복", 7, 1))
            )
        )

        mockMvc.perform(get("/member/emotion/base-analysis"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.Relation[0].emotion").value("친밀"))
            .andExpect(jsonPath("$.Self[0].count").value(1))
            .andExpect(jsonPath("$.State[0].intensity").value(7))
    }

    @Test
    @DisplayName("GET /emotion/negative: 부정 감정 분석 배열 계약을 반환한다")
    fun `negative emotion returns grouped arrays`() {
        whenever(aggregationService.targetSummary(member.id, EmotionGroup.스트레스, 7)).thenReturn(emptyList())
        whenever(aggregationService.targetSummary(member.id, EmotionGroup.우울, 7)).thenReturn(emptyList())
        whenever(aggregationService.targetSummary(member.id, EmotionGroup.불안, 7)).thenReturn(emptyList())
        whenever(aggregationService.dateSummary(member.id, EmotionGroup.스트레스, 7)).thenReturn(emptyList())
        whenever(aggregationService.dateSummary(member.id, EmotionGroup.우울, 7)).thenReturn(emptyList())
        whenever(aggregationService.dateSummary(member.id, EmotionGroup.불안, 7)).thenReturn(emptyList())

        mockMvc.perform(get("/emotion/negative").param("period", "7"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.stressTarget").isArray)
            .andExpect(jsonPath("$.depressionDate").isArray)
            .andExpect(jsonPath("$.anxietyTarget").isArray)
    }

    @Test
    @DisplayName("GET /relation: 관계 그래프 호환 계약을 반환한다")
    fun `relation returns graph contract`() {
        whenever(aggregationService.relationGraph(member.id)).thenReturn(
            GraphResponse(
                todayEmotions = listOf(GraphEmotionResponse("행복", 7)),
                relations = WillBeDeprecatedDTO(emptyList())
            )
        )

        mockMvc.perform(get("/relation"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.todayMyEmotions[0].emotion").value("행복"))
            .andExpect(jsonPath("$.relations.relations").isArray)
    }

    @Test
    @DisplayName("GET /strength/period: 강점 월별 계약을 반환한다")
    fun `strength period returns strength contract`() {
        whenever(aggregationService.strength(member.id, 2026, 6)).thenReturn(
            StrengthResponse(
                typeCount = mapOf("창의성" to 2),
                detailCount = mapOf("창의성" to mapOf("아이디어" to 2))
            )
        )

        mockMvc.perform(get("/strength/period").param("year", "2026").param("month", "6"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.typeCount.창의성").value(2))
            .andExpect(jsonPath("$.detailCount.창의성.아이디어").value(2))
    }

    private class FixedCurrentMemberResolver(
        private val member: Member
    ) : HandlerMethodArgumentResolver {

        override fun supportsParameter(parameter: MethodParameter): Boolean =
            parameter.hasParameterAnnotation(CurrentMember::class.java)

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any = member
    }
}
