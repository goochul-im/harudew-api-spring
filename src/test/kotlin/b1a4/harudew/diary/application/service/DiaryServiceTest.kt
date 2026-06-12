package b1a4.harudew.diary.application.service

import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand
import b1a4.harudew.diary.adapter.exception.DiaryAnalysisFailedException
import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisPort
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.diary.application.port.out.analysis.Reflection
import b1a4.harudew.diary.domain.event.DiaryCreateEvent
import b1a4.harudew.diary.domain.model.Diary
import b1a4.harudew.global.event.DomainEventPublisherPort
import b1a4.harudew.global.exception.ErrorCode
import b1a4.harudew.global.infrastructure.storage.FileUploadRequest
import b1a4.harudew.global.infrastructure.storage.StorageClientPort
import b1a4.harudew.member.application.port.out.MemberRepository
import b1a4.harudew.member.domain.Member
import b1a4.harudew.member.domain.SocialType
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockMultipartFile
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class DiaryServiceTest {

    @Mock lateinit var storageClient: StorageClientPort
    @Mock lateinit var diaryAnalysisPort: DiaryAnalysisPort
    @Mock lateinit var eventPublisher: DomainEventPublisherPort
    @Mock lateinit var diaryRepository: DiaryRepository
    @Mock lateinit var memberRepository: MemberRepository

    private lateinit var service: DiaryService

    @BeforeEach
    fun setUp() {
        service = DiaryService(
            storageClient = storageClient,
            diaryAnalysisAdapter = diaryAnalysisPort,
            eventPublisher = eventPublisher,
            diaryRepository = diaryRepository,
            memberRepository = memberRepository
        )
    }

    @Test
    @DisplayName("create: 파일 업로드, AI 분석, 일기 저장, 생성 이벤트 발행을 수행하고 ID를 반환한다")
    fun `create saves diary and publishes create event`() = runBlocking {
        // given
        val member = Member(
            id = "member-1",
            email = "member@test.local",
            nickname = "테스터",
            socialType = SocialType.GOOGLE,
            character = "test"
        )
        val analysis = DiaryAnalysisResponse(
            activityAnalysis = emptyList(),
            reflection = Reflection(
                achievements = listOf("기록 완료"),
                shortcomings = emptyList(),
                todo = listOf("내일 산책")
            )
        )
        val command = CreateDiaryCommand(
            writtenDate = LocalDate.of(2026, 6, 12),
            content = "오늘은 테스트 일기를 작성했다.",
            weather = "SUNNY",
            latitude = 37.1,
            longitude = 127.2
        )
        val photo = MockMultipartFile("photo", "photo.jpg", "image/jpeg", byteArrayOf(1, 2))
        val audio = MockMultipartFile("audios", "voice.mp3", "audio/mpeg", byteArrayOf(3, 4))

        whenever(memberRepository.findById(member.id)).thenReturn(member)
        whenever(diaryAnalysisPort.getAnalysis(command.content)).thenReturn(analysis)
        whenever(storageClient.upload(any<FileUploadRequest>()))
            .thenReturn("https://cdn.test/photo.jpg", "https://cdn.test/voice.mp3")
        whenever(diaryRepository.save(any())).thenAnswer { invocation ->
            val diary = invocation.arguments[0] as Diary
            Diary(
                id = 100L,
                author = diary.author,
                writtenDate = diary.writtenDate,
                content = diary.content,
                title = diary.title,
                weather = diary.weather,
                photoPath = diary.photoPath,
                audioPath = diary.audioPath,
                isBookmark = diary.isBookmark,
                latitude = diary.latitude,
                longitude = diary.longitude,
                metaData = diary.metaData
            )
        }

        // when
        val diaryId = service.create(
            authorId = member.id,
            command = command,
            photos = listOf(photo),
            audios = listOf(audio)
        )

        // then
        assertThat(diaryId).isEqualTo(100L)

        val diaryCaptor = argumentCaptor<Diary>()
        verify(diaryRepository).save(diaryCaptor.capture())
        assertThat(diaryCaptor.firstValue.writtenDate).isEqualTo(LocalDate.of(2026, 6, 12))
        assertThat(diaryCaptor.firstValue.weather).isEqualTo("SUNNY")
        assertThat(diaryCaptor.firstValue.photoPath).containsExactly("https://cdn.test/photo.jpg")
        assertThat(diaryCaptor.firstValue.audioPath).containsExactly("https://cdn.test/voice.mp3")
        assertThat(diaryCaptor.firstValue.metaData).isEqualTo(analysis)

        val eventCaptor = argumentCaptor<DiaryCreateEvent>()
        verify(eventPublisher).publish(eventCaptor.capture())
        assertThat(eventCaptor.firstValue.diaryId).isEqualTo(100L)
        assertThat(eventCaptor.firstValue.authorId).isEqualTo("member-1")
        assertThat(eventCaptor.firstValue.writtenDate).isEqualTo(LocalDate.of(2026, 6, 12))
        assertThat(eventCaptor.firstValue.analysisResult).isEqualTo(analysis)
    }

    @Test
    @DisplayName("create: AI 분석 실패 시 일기 저장과 이벤트 발행을 하지 않는다")
    fun `create does not save diary when analysis fails`() {
        // given
        val command = CreateDiaryCommand(
            writtenDate = LocalDate.of(2026, 6, 12),
            content = "분석 실패 일기"
        )
        whenever(diaryAnalysisPort.getAnalysis(command.content))
            .thenThrow(DiaryAnalysisFailedException(RuntimeException("model failed"), mapOf("content" to command.content)))

        // when & then
        assertThatThrownBy {
            runBlocking {
                service.create(
                    authorId = "member-1",
                    command = command,
                    photos = null,
                    audios = null
                )
            }
        }.hasMessage(ErrorCode.DIARY_ANALYSIS_FAILED.message)

        verify(diaryRepository, never()).save(any())
        verify(eventPublisher, never()).publish(any())
    }
}
