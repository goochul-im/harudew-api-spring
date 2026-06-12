package b1a4.harudew.diary.application.service

import b1a4.harudew.activity.adapter.dto.response.ActivitySimpleResponse
import b1a4.harudew.diary.adapter.dto.request.CreateDiaryCommand
import b1a4.harudew.diary.adapter.dto.response.DiaryAnalysisResult
import b1a4.harudew.diary.adapter.dto.response.BookmarkToggleResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryDateResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryDetailResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryEmotionScoreResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryHomeResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryInfoFields
import b1a4.harudew.diary.adapter.dto.response.DiaryMapInfo
import b1a4.harudew.diary.adapter.dto.response.DiaryMapResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPageResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPhotoResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryPhotosResponse
import b1a4.harudew.diary.adapter.dto.response.DiaryResponse
import b1a4.harudew.diary.adapter.dto.response.DiarySearchResponse
import b1a4.harudew.diary.adapter.dto.response.EmotionAnalysisResponse
import b1a4.harudew.diary.adapter.dto.response.EmotionScoreResponse
import b1a4.harudew.diary.adapter.dto.response.JsonActivityResponse
import b1a4.harudew.diary.adapter.dto.response.JsonEmotionResponse
import b1a4.harudew.diary.adapter.dto.response.JsonPeopleResponse
import b1a4.harudew.diary.adapter.dto.response.JsonProblemAnalysis
import b1a4.harudew.diary.adapter.dto.response.JsonReflectionResponse
import b1a4.harudew.diary.adapter.dto.response.JsonResponse
import b1a4.harudew.diary.adapter.dto.response.SearchItems
import b1a4.harudew.diary.adapter.dto.response.WrittenDaysResponse
import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.diary.application.port.`in`.DiaryCommandUseCase
import b1a4.harudew.diary.application.port.`in`.DiaryQueryUseCase
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisPort
import b1a4.harudew.diary.application.port.out.analysis.ActivityAnalysis
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.diary.application.port.out.analysis.EmotionData
import b1a4.harudew.diary.application.port.out.analysis.PersonAnalysis
import b1a4.harudew.diary.application.port.out.analysis.ProblemAnalysis
import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.diary.domain.event.DiaryCreateEvent
import b1a4.harudew.diary.domain.model.Diary
import b1a4.harudew.global.event.DomainEventPublisherPort
import b1a4.harudew.global.infrastructure.storage.FileUploadRequest
import b1a4.harudew.global.infrastructure.storage.StorageClientPort
import b1a4.harudew.member.application.port.out.MemberRepository
import b1a4.harudew.member.domain.Member
import b1a4.harudew.person.dto.response.PeopleAnalysisResponse
import b1a4.harudew.todo.adapter.dto.response.TodoAnalysisResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate

@Service
class DiaryService(
    private val storageClient: StorageClientPort,
    private val diaryAnalysisAdapter: DiaryAnalysisPort,
    private val eventPublisher: DomainEventPublisherPort,
    private val diaryRepository: DiaryRepository,
    private val memberRepository: MemberRepository
) : DiaryCommandUseCase, DiaryQueryUseCase {

    override suspend fun create(
        authorId: String,
        command: CreateDiaryCommand,
        photos: List<MultipartFile>?,
        audios: List<MultipartFile>?
    ): Long {
        val uploadAndAnalysis = coroutineScope {
            val photos = async { uploadFiles(photos) }
            val audios = async { uploadFiles(audios) }
            val diaryAnalysisResult = async { analysisDiary(command.content) }
            UploadAndAnalysis(
                photoUrls = photos.await(),
                audioUrls = audios.await(),
                analysisResult = diaryAnalysisResult.await()
            )
        }

        val author = memberRepository.findById(authorId)
        val result = Diary(
            author = author,
            writtenDate = command.writtenDate,
            photoPath = uploadAndAnalysis.photoUrls,
            latitude = command.latitude,
            longitude = command.longitude,
            audioPath = uploadAndAnalysis.audioUrls,
            metaData = uploadAndAnalysis.analysisResult,
            content = command.content,
            weather = command.weather,
        )

        val saveDiary = diaryRepository.save(result)
        val diaryId = saveDiary.id ?: throw IllegalStateException("저장된 일기 ID가 없습니다.")
        eventPublisher.publish(
            DiaryCreateEvent(
                diaryId,
                command.content,
                analysisResult = uploadAndAnalysis.analysisResult,
                authorId = authorId,
                writtenDate = command.writtenDate,
            )
        )

        return diaryId
    }

    override fun delete(diaryId: Long, member: Member) {
        diaryRepository.deleteByIdAndAuthorId(diaryId, member.id)
    }

    override fun toggleBookmark(diaryId: Long, member: Member): BookmarkToggleResponse {
        val diary = diaryRepository.toggleBookmark(diaryId, member.id)
        return BookmarkToggleResponse(id = diary.id!!, isBookmarked = diary.isBookmark)
    }

    override fun findById(
        diaryId: Long,
        member: Member
    ): DiaryAnalysisResult {
        val diary = diaryRepository.findByIdAndAuthorId(diaryId, member.id)
        val analysis = normalizeAnalysis(diary.metaData)
        return DiaryAnalysisResult(
            id = diary.id!!,
            title = diary.title,
            photoPath = diary.photoPath,
            content = diary.content,
            people = toPeopleResponses(analysis),
            selfEmotions = toEmotionResponses(analysis.activityAnalysis.flatMap { it.selfEmotions }),
            stateEmotions = toEmotionResponses(analysis.activityAnalysis.flatMap { it.stateEmotions }),
            activities = analysis.activityAnalysis.map { ActivitySimpleResponse(it.activity, it.strength) },
            todos = analysis.reflection.todo.map { TodoAnalysisResponse(it) }
        )
    }

    override fun findJsonById(
        diaryId: Long,
        member: Member
    ): DiaryDetailResponse {
        val diary = diaryRepository.findByIdAndAuthorId(diaryId, member.id)
        val analysis = normalizeAnalysis(diary.metaData)
        return DiaryDetailResponse(
            id = diary.id!!,
            writtenDate = diary.writtenDate,
            photoPath = diary.photoPath,
            audioPath = diary.audioPath,
            isBookmarked = diary.isBookmark,
            people = toPeopleResponses(analysis),
            content = diary.content,
            emotions = toEmotionResponses(analysis.activityAnalysis.flatMap { it.selfEmotions + it.stateEmotions }),
            latitude = diary.latitude,
            longitude = diary.longitude,
            stressWarning = false,
            anxietyWarning = false,
            depressionWarning = false,
            recommendRoutine = null,
            beforeDiaryScores = DiaryEmotionScoreResponse(emptyList()),
            analysis = toFrontendJson(analysis)
        )
    }

    override fun findByDate(
        member: Member,
        date: LocalDate
    ): DiaryDateResponse {
        val diaries = diaryRepository.findByAuthorIdAndWrittenDate(member.id, date).map(::toDiaryResponse)
        return DiaryDateResponse(diaries = diaries)
    }

    override fun search(
        member: Member,
        query: String
    ): DiarySearchResponse {
        val diaries = diaryRepository.searchByAuthorId(member.id, query).map { diary ->
            SearchItems(
                fields = toDiaryInfoFields(diary),
                searchSentence = query,
                relateSentence = diary.content
            )
        }
        return DiarySearchResponse(diaries = diaries, totalCount = diaries.size)
    }

    override fun findAll(member: Member, cursor: Long?, limit: Int): DiaryPageResponse {
        val pageSize = limit.coerceIn(1, 50)
        val diaries = diaryRepository.findByAuthorId(member.id, cursor, pageSize + 1)
        return toPageResponse(diaries, pageSize, diaryRepository.countByAuthorId(member.id).toInt())
    }

    override fun findBookmark(member: Member, page: Int, limit: Int): DiaryPageResponse {
        val pageSize = limit.coerceIn(1, 50)
        val diaries = diaryRepository.findBookmarksByAuthorId(member.id, page, pageSize + 1)
        return toPageResponse(diaries, pageSize, diaryRepository.countByAuthorId(member.id).toInt())
    }

    override fun findPhotos(member: Member, cursor: Long?, limit: Int): DiaryPhotosResponse {
        val pageSize = limit.coerceIn(1, 50)
        val diaries = diaryRepository.findPhotosByAuthorId(member.id, cursor, pageSize + 1)
        val visible = diaries.take(pageSize)
        return DiaryPhotosResponse(
            photos = visible.flatMap { diary ->
                diary.photoPath.map { photo ->
                    DiaryPhotoResponse(
                        diaryId = diary.id!!,
                        writtenDate = diary.writtenDate,
                        photoPath = photo,
                        content = diary.content
                    )
                }
            },
            hasMore = diaries.size > pageSize,
            nextCursor = visible.lastOrNull()?.id
        )
    }

    override fun findWrittenDays(member: Member, year: Int, month: Int): WrittenDaysResponse =
        WrittenDaysResponse(diaryRepository.findWrittenDays(member.id, year, month))

    override fun findMap(member: Member): DiaryMapResponse {
        return DiaryMapResponse(
            result = diaryRepository.findMapDiaries(member.id).map { diary ->
                DiaryMapInfo(
                    latitude = diary.latitude!!,
                    longitude = diary.longitude!!,
                    diaryId = diary.id!!,
                    photoPath = diary.photoPath.firstOrNull(),
                    content = diary.content
                )
            }
        )
    }

    override fun findEmotionScores(diaryId: Long, member: Member, period: Int): DiaryEmotionScoreResponse {
        val anchor = diaryRepository.findByIdAndAuthorId(diaryId, member.id)
        val days = period.coerceIn(1, 365)
        val diaries = (0L until days).flatMap { offset ->
            diaryRepository.findByAuthorIdAndWrittenDate(member.id, anchor.writtenDate.minusDays(offset))
        }
        return DiaryEmotionScoreResponse(
            scores = diaries.map { diary ->
                EmotionScoreResponse(
                    id = diary.id!!,
                    writtenDate = diary.writtenDate,
                    intensitySum = sumEmotionIntensity(normalizeAnalysis(diary.metaData))
                )
            }.sortedBy { it.writtenDate }
        )
    }

    private suspend fun analysisDiary(content: String): DiaryAnalysisResponse =
        withContext(Dispatchers.IO) {
            diaryAnalysisAdapter.getAnalysis(content)
        }

    /**
     * 코루틴을 이용해 여러 파일을 병렬로 업로드
     */
    private suspend fun uploadFiles(files: List<MultipartFile>?): List<String> =
        coroutineScope {
            files?.map { file ->
                async(Dispatchers.IO) {
                    storageClient.upload(
                        FileUploadRequest(
                            bytes = file.bytes,
                            contentType = file.contentType ?: "application/octet-stream",
                            originalFilename = file.originalFilename,
                        )
                    )
                }
            }?.awaitAll() ?: emptyList()
        }

    private data class UploadAndAnalysis(
        val photoUrls: List<String>,
        val audioUrls: List<String>,
        val analysisResult: DiaryAnalysisResponse
    )

    private fun toPageResponse(diaries: List<Diary>, pageSize: Int, totalCount: Int): DiaryPageResponse {
        val visible = diaries.take(pageSize)
        return DiaryPageResponse(
            item = DiaryHomeResponse(
                diaries = visible.map(::toDiaryResponse),
                continuousWritingDate = calculateContinuousWritingDate(visible),
                totalDiaryCount = totalCount,
                emotionCountByMonth = visible.flatMap { extractEmotionNames(normalizeAnalysis(it.metaData)) }.distinct().size
            ),
            hasMore = diaries.size > pageSize,
            nextCursor = visible.lastOrNull()?.id
        )
    }

    private fun toDiaryResponse(diary: Diary): DiaryResponse {
        return DiaryResponse(
            toDiaryInfoFields(diary)
        )
    }

    private fun toDiaryInfoFields(diary: Diary): DiaryInfoFields {
        val analysis = normalizeAnalysis(diary.metaData)
        return SimpleDiaryInfoFields(
            diaryId = diary.id!!,
            title = diary.title,
            writtenDate = diary.writtenDate,
            content = diary.content,
            photoPath = diary.photoPath,
            audioPath = diary.audioPath,
            isBookmarked = diary.isBookmark,
            latitude = diary.latitude,
            longitude = diary.longitude,
            activities = analysis.activityAnalysis.map { it.activity },
            emotions = extractEmotionNames(analysis),
            people = analysis.activityAnalysis.flatMap { it.peoples.map { person -> person.name } }.distinct(),
            targets = analysis.activityAnalysis.flatMap { it.peoples.map { person -> person.name } }.distinct()
        )
    }

    private fun normalizeAnalysis(metaData: Any): DiaryAnalysisResponse {
        if (metaData is DiaryAnalysisResponse) return metaData
        if (metaData is Map<*, *>) {
            val activities = metaData.listValue("activity_analysis", "activityAnalysis").mapNotNull(::activityFromMap)
            val reflectionMap = metaData.mapValue("reflection")
            return DiaryAnalysisResponse(
                activityAnalysis = activities,
                reflection = b1a4.harudew.diary.application.port.out.analysis.Reflection(
                    achievements = reflectionMap.stringList("achievements", "achievement"),
                    shortcomings = reflectionMap.stringList("shortcomings"),
                    todo = reflectionMap.stringList("todo", "todos"),
                    tomorrowMindSet = reflectionMap.stringValue("tomorrow_mindset", "tomorrowMindSet", "tomorrowMindset")
                )
            )
        }
        return DiaryAnalysisResponse(
            activityAnalysis = emptyList(),
            reflection = b1a4.harudew.diary.application.port.out.analysis.Reflection(
                achievements = emptyList(),
                shortcomings = emptyList(),
                todo = emptyList()
            )
        )
    }

    private fun activityFromMap(value: Any?): ActivityAnalysis? {
        val map = value as? Map<*, *> ?: return null
        return ActivityAnalysis(
            activity = map.stringValue("activity") ?: "",
            peoples = map.listValue("peoples", "people").mapNotNull(::personFromMap),
            selfEmotions = map.emotionList("self_emotions", "selfEmotions"),
            stateEmotions = map.emotionList("state_emotions", "stateEmotions"),
            problem = map.listValue("problem").mapNotNull(::problemFromMap),
            strength = map.stringValue("strength") ?: ""
        )
    }

    private fun personFromMap(value: Any?): PersonAnalysis? {
        val map = value as? Map<*, *> ?: return null
        return PersonAnalysis(
            name = map.stringValue("name") ?: "",
            interactions = map.emotionList("interactions"),
            nameIntimacy = map.stringValue("name_intimacy", "nameIntimacy") ?: "0"
        )
    }

    private fun problemFromMap(value: Any?): ProblemAnalysis? {
        val map = value as? Map<*, *> ?: return null
        return ProblemAnalysis(
            situation = map.stringValue("situation") ?: "",
            approach = map.stringValue("approach") ?: "",
            outcome = map.stringValue("outcome") ?: "",
            conflictResponseCode = map.stringValue("conflict_response_code", "conflictResponseCode") ?: ""
        )
    }

    private fun toFrontendJson(analysis: DiaryAnalysisResponse): JsonResponse =
        JsonResponse(
            activityAnalysis = analysis.activityAnalysis.map { activity ->
                JsonActivityResponse(
                    activityName = activity.activity,
                    peoples = activity.peoples.map { person ->
                        JsonPeopleResponse(
                            name = person.name,
                            interactions = toJsonEmotion(person.interactions),
                            nameIntensity = person.nameIntimacy.toIntOrNull() ?: 0
                        )
                    },
                    selfEmotions = toJsonEmotion(activity.selfEmotions),
                    stateEmotions = toJsonEmotion(activity.stateEmotions),
                    problem = activity.problem.map { problem ->
                        JsonProblemAnalysis(
                            situation = problem.situation,
                            approach = problem.approach,
                            outcome = problem.outcome,
                            conflictResponseCode = problem.conflictResponseCode
                        )
                    },
                    strength = activity.strength
                )
            },
            reflection = JsonReflectionResponse(
                achievements = analysis.reflection.achievements,
                shortcomings = analysis.reflection.shortcomings,
                tomorrowMindset = analysis.reflection.tomorrowMindSet.orEmpty(),
                todos = analysis.reflection.todo
            )
        )

    private fun toPeopleResponses(analysis: DiaryAnalysisResponse): List<PeopleAnalysisResponse> =
        analysis.activityAnalysis
            .flatMap { it.peoples }
            .groupBy { it.name }
            .map { (name, people) ->
                PeopleAnalysisResponse(
                    name = name,
                    feel = toEmotionResponses(people.flatMap { it.interactions }),
                    count = people.size
                )
            }

    private fun toEmotionResponses(emotions: List<EmotionData>): List<EmotionAnalysisResponse> =
        emotions.mapNotNull { emotion ->
            EmotionType.entries.firstOrNull { it.name == emotion.emotion }
                ?.let { EmotionAnalysisResponse(it, emotion.emotionIntensity) }
        }

    private fun toJsonEmotion(emotions: List<EmotionData>): JsonEmotionResponse =
        JsonEmotionResponse(
            emotion = emotions.map { it.emotion },
            emotionIntensity = emotions.map { it.emotionIntensity }
        )

    private fun extractEmotionNames(analysis: DiaryAnalysisResponse): List<String> =
        analysis.activityAnalysis
            .flatMap { activity -> activity.selfEmotions + activity.stateEmotions + activity.peoples.flatMap { it.interactions } }
            .map { it.emotion }
            .distinct()

    private fun sumEmotionIntensity(analysis: DiaryAnalysisResponse): Int =
        analysis.activityAnalysis
            .flatMap { activity -> activity.selfEmotions + activity.stateEmotions }
            .sumOf { it.emotionIntensity }

    private fun calculateContinuousWritingDate(diaries: List<Diary>): Int {
        val dates = diaries.map { it.writtenDate }.toSet()
        if (dates.isEmpty()) return 0
        var current = dates.max()
        var count = 0
        while (dates.contains(current)) {
            count += 1
            current = current.minusDays(1)
        }
        return count
    }

    private fun Map<*, *>.mapValue(key: String): Map<*, *> =
        this[key] as? Map<*, *> ?: emptyMap<Any, Any>()

    private fun Map<*, *>.listValue(vararg keys: String): List<Any?> {
        val raw = keys.firstNotNullOfOrNull { this[it] } ?: return emptyList()
        return raw as? List<Any?> ?: emptyList()
    }

    private fun Map<*, *>.stringList(vararg keys: String): List<String> =
        listValue(*keys).mapNotNull { it?.toString() }

    private fun Map<*, *>.stringValue(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { this[it]?.toString() }

    private fun Map<*, *>.emotionList(vararg keys: String): List<EmotionData> {
        val raw = keys.firstNotNullOfOrNull { this[it] } ?: return emptyList()
        if (raw is Map<*, *>) {
            val emotionNames = raw.stringList("emotion")
            val intensities = raw.listValue("emotion_intensity", "intensity")
                .map { it.toString().toIntOrNull() ?: 0 }
            return emotionNames.mapIndexed { index, emotion ->
                EmotionData(emotion = emotion, emotionIntensity = intensities.getOrElse(index) { 0 })
            }
        }
        return (raw as? List<*>)?.mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            val emotion = map.stringValue("emotion") ?: return@mapNotNull null
            EmotionData(
                emotion = emotion,
                emotionIntensity = map.stringValue("emotion_intensity", "emotionIntensity", "intensity")?.toIntOrNull() ?: 0
            )
        } ?: emptyList()
    }

    private data class SimpleDiaryInfoFields(
        override val diaryId: Long,
        override val title: String,
        override val writtenDate: LocalDate,
        override val content: String,
        override val photoPath: List<String>?,
        override val audioPath: List<String>?,
        override val isBookmarked: Boolean,
        override val latitude: Number?,
        override val longitude: Number?,
        override val activities: List<String>?,
        override val emotions: List<String>?,
        override val people: List<String>?,
        override val targets: List<String>?
    ) : DiaryInfoFields
}
