package b1a4.harudew.diary.adapter.`in`.web

import b1a4.harudew.auth.annotation.MemberId
import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand
import b1a4.harudew.diary.application.port.`in`.DiaryCommandUseCase
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import java.time.LocalDate

class DiaryControllerTest {

    private lateinit var diaryCommandUseCase: DiaryCommandUseCase
    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        diaryCommandUseCase = mock()
        mockMvc = MockMvcBuilders
            .standaloneSetup(DiaryController(diaryCommandUseCase))
            .setCustomArgumentResolvers(FixedMemberIdResolver("member-1"))
            .build()
    }

    @Test
    @DisplayName("POST /diary: 프론트 multipart 계약을 받아 일기 생성 ID를 반환한다")
    fun `create accepts frontend multipart contract`() {
        // given
        runBlocking {
            whenever(
                diaryCommandUseCase.create(
                    eq("member-1"),
                    org.mockito.kotlin.any(),
                    org.mockito.kotlin.any(),
                    org.mockito.kotlin.any()
                )
            ).thenReturn(55L)
        }
        val photo = MockMultipartFile("photo", "photo.jpg", "image/jpeg", byteArrayOf(1))
        val audio = MockMultipartFile("audios", "voice.mp3", "audio/mpeg", byteArrayOf(2))

        // when & then
        mockMvc.perform(
            multipart("/diary")
                .file(photo)
                .file(audio)
                .param("content", "오늘은 프론트에서 일기를 작성했다.")
                .param("writtenDate", "2026-06-12")
                .param("weather", "SUNNY")
                .param("latitude", "37.5665")
                .param("longitude", "126.978")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(55))

        val commandCaptor = argumentCaptor<CreateDiaryCommand>()
        runBlocking {
            verify(diaryCommandUseCase).create(
                eq("member-1"),
                commandCaptor.capture(),
                org.mockito.kotlin.any(),
                org.mockito.kotlin.any()
            )
        }
        assertThat(commandCaptor.firstValue.content).isEqualTo("오늘은 프론트에서 일기를 작성했다.")
        assertThat(commandCaptor.firstValue.writtenDate).isEqualTo(LocalDate.of(2026, 6, 12))
        assertThat(commandCaptor.firstValue.weather).isEqualTo("SUNNY")
        assertThat(commandCaptor.firstValue.latitude).isEqualTo(37.5665)
        assertThat(commandCaptor.firstValue.longitude).isEqualTo(126.978)
    }

    private class FixedMemberIdResolver(
        private val memberId: String
    ) : HandlerMethodArgumentResolver {

        override fun supportsParameter(parameter: MethodParameter): Boolean {
            return parameter.hasParameterAnnotation(MemberId::class.java)
        }

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ): Any {
            return memberId
        }
    }
}
