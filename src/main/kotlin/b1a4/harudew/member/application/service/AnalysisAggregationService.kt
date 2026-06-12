package b1a4.harudew.member.application.service

import b1a4.harudew.diary.adapter.dto.response.DiaryInfoFields
import b1a4.harudew.diary.adapter.dto.response.DiaryResponse
import b1a4.harudew.diary.application.port.out.DiaryRepository
import b1a4.harudew.diary.application.port.out.analysis.ActivityAnalysis
import b1a4.harudew.diary.application.port.out.analysis.DiaryAnalysisResponse
import b1a4.harudew.diary.application.port.out.analysis.EmotionData
import b1a4.harudew.diary.application.port.out.analysis.PersonAnalysis
import b1a4.harudew.diary.application.port.out.analysis.ProblemAnalysis
import b1a4.harudew.diary.application.port.out.analysis.Reflection
import b1a4.harudew.diary.domain.model.Diary
import b1a4.harudew.emotion.adapter.dto.response.ActivityEmotionSummaryResponse
import b1a4.harudew.emotion.adapter.dto.response.EmotionDetailResponse
import b1a4.harudew.emotion.adapter.dto.response.EmotionSummaryPeriodResponse
import b1a4.harudew.emotion.adapter.dto.response.GraphEmotionResponse
import b1a4.harudew.emotion.adapter.dto.response.GraphResponse
import b1a4.harudew.emotion.adapter.dto.response.TargetActivityResponse
import b1a4.harudew.emotion.adapter.dto.response.TargetDetailAnalysisResponse
import b1a4.harudew.emotion.adapter.dto.response.TargetEmotionResponse
import b1a4.harudew.emotion.adapter.dto.response.TargetEmotionSummaryResponse
import b1a4.harudew.emotion.adapter.dto.response.WillBeDeprecatedDTO
import b1a4.harudew.emotion.domain.EmotionBase
import b1a4.harudew.emotion.domain.EmotionGroup
import b1a4.harudew.emotion.domain.EmotionType
import b1a4.harudew.emotion.domain.getEmotionBase
import b1a4.harudew.emotion.domain.toEmotionGroup
import b1a4.harudew.member.adapter.dto.response.EmotionBaseAnalysis
import b1a4.harudew.member.adapter.dto.response.EmotionBaseAnalysisRes
import b1a4.harudew.member.adapter.dto.response.EmotionGroups
import b1a4.harudew.member.adapter.dto.response.MemberSummaryRes
import b1a4.harudew.member.adapter.dto.response.PerDate
import b1a4.harudew.member.adapter.dto.response.StrengthResponse
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AnalysisAggregationService(
    private val diaryRepository: DiaryRepository
) {

    fun emotionBaseAnalysis(memberId: String, year: Int? = null, month: Int? = null): EmotionBaseAnalysisRes {
        val entries = analysisEntries(memberId)
            .filter { (_, diary) ->
                year == null || month == null || (diary.writtenDate.year == year && diary.writtenDate.monthValue == month)
            }
            .flatMap { (analysis, _) -> allEmotionEntries(analysis) }

        fun byBase(base: EmotionBase) = entries
            .filter { it.type.getEmotionBase() == base }
            .groupBy { it.type }
            .map { (emotion, values) ->
                EmotionBaseAnalysis(
                    emotion = emotion.name,
                    intensity = values.sumOf { it.intensity },
                    count = values.size
                )
            }
            .sortedByDescending { it.count.toInt() }

        return EmotionBaseAnalysisRes(
            relation = byBase(EmotionBase.RELATION),
            self = byBase(EmotionBase.SELF),
            state = byBase(EmotionBase.STATE)
        )
    }

    fun memberSummary(memberId: String, period: Int): MemberSummaryRes {
        val since = LocalDate.now().minusDays(period.toLong())
        val perDate = analysisEntries(memberId)
            .filter { (_, diary) -> !diary.writtenDate.isBefore(since) }
            .groupBy { it.second.writtenDate }
            .map { (date, entries) ->
                val groups = entries
                    .flatMap { (analysis, _) -> allEmotionEntries(analysis) }
                    .groupBy { it.type.toEmotionGroup() }
                    .map { (group, values) -> EmotionGroups(group, values.sumOf { it.intensity }) }
                PerDate(date = date, emotions = groups)
            }
            .sortedBy { it.date }

        val totals = perDate.flatMap { it.emotions }.groupBy { it.emotion }
            .mapValues { (_, values) -> values.sumOf { it.intensity.toInt() } }
        return MemberSummaryRes(
            depressionWarning = (totals[EmotionGroup.우울] ?: 0) >= 30,
            stressWarning = (totals[EmotionGroup.스트레스] ?: 0) >= 30,
            anxietyWarning = (totals[EmotionGroup.불안] ?: 0) >= 30,
            period = period,
            emotionPerDate = perDate
        )
    }

    fun character(memberId: String): String {
        val base = emotionBaseAnalysis(memberId)
        val dominant = listOf(
            "호랑이" to base.relation.sumOf { it.intensity.toInt() },
            "고양이" to base.self.sumOf { it.intensity.toInt() },
            "고래" to base.state.sumOf { it.intensity.toInt() }
        ).maxByOrNull { it.second }
        return dominant?.takeIf { it.second > 0 }?.first ?: "unknown"
    }

    fun relationGraph(memberId: String): GraphResponse {
        val entries = analysisEntries(memberId)
        val targetStats = entries
            .flatMap { (analysis, _) -> analysis.activityAnalysis.flatMap { it.peoples } }
            .filter { it.name.isNotBlank() && it.name.lowercase() != "none" }
            .groupBy { it.name }
            .entries
            .mapIndexed { index, entry ->
                val emotions = entry.value.flatMap { it.interactions }.toEmotionDetails()
                TargetEmotionResponse(
                    id = index + 1L,
                    name = entry.key,
                    affection = entry.value.sumOf { it.nameIntimacy.toIntOrNull() ?: 0 }.coerceIn(0, 100),
                    emotions = emotions,
                    count = entry.value.size
                )
            }

        val myEmotions = entries.flatMap { (analysis, _) ->
            analysis.activityAnalysis.flatMap { it.selfEmotions + it.stateEmotions }
        }.take(5).map { GraphEmotionResponse(it.emotion, it.emotionIntensity) }

        return GraphResponse(
            todayEmotions = myEmotions,
            relations = WillBeDeprecatedDTO(targetStats)
        )
    }

    fun relationDetail(memberId: String, targetId: Long): TargetDetailAnalysisResponse {
        val graph = relationGraph(memberId)
        val target = graph.relations.relations.firstOrNull { it.id == targetId }
            ?: TargetEmotionResponse(targetId, "unknown", 0, emptyList(), 0)
        val targetName = target.name
        val matching = analysisEntries(memberId).filter { (analysis, _) ->
            analysis.activityAnalysis.any { activity ->
                activity.peoples.any { it.name == targetName }
            }
        }
        val activities = matching
            .flatMap { (analysis, _) -> analysis.activityAnalysis.map { it.activity } }
            .groupingBy { it }
            .eachCount()
            .map { TargetActivityResponse(content = it.key, count = it.value) }
        val diaries = matching.map { (analysis, diary) -> toDiaryResponse(diary, analysis) }

        return TargetDetailAnalysisResponse(
            id = targetId,
            name = targetName,
            closenessScore = target.affection.toInt(),
            emotions = target.emotions,
            diaries = diaries,
            activities = activities
        )
    }

    fun targetSummary(memberId: String, group: EmotionGroup? = null, period: Int = 180): List<TargetEmotionSummaryResponse> {
        val since = LocalDate.now().minusDays(period.toLong())
        return analysisEntries(memberId)
            .filter { (_, diary) -> !diary.writtenDate.isBefore(since) }
            .flatMap { (analysis, _) ->
                analysis.activityAnalysis.flatMap { activity ->
                    activity.peoples.flatMap { person ->
                        person.interactions.mapNotNull { emotion ->
                            val type = emotion.toType() ?: return@mapNotNull null
                            if (group != null && type.toEmotionGroup() != group) return@mapNotNull null
                            PersonEmotionAggregate(person.name, type.toEmotionGroup(), emotion.emotionIntensity)
                        }
                    }
                }
            }
            .filter { it.name.isNotBlank() && it.name.lowercase() != "none" }
            .groupBy { it.name to it.group }
            .entries
            .mapIndexed { index, entry ->
                TargetEmotionSummaryResponse(
                    targetId = index + 1L,
                    targetName = entry.key.first,
                    emotion = entry.key.second,
                    totalIntensity = entry.value.sumOf { it.intensity }.toLong(),
                    percentage = entry.value.size
                )
            }
            .sortedByDescending { it.totalIntensity }
    }

    fun activitySummary(memberId: String, group: EmotionGroup, period: Int): List<ActivityEmotionSummaryResponse> {
        val since = LocalDate.now().minusDays(period.toLong())
        return analysisEntries(memberId)
            .filter { (_, diary) -> !diary.writtenDate.isBefore(since) }
            .flatMap { (analysis, _) ->
                analysis.activityAnalysis.flatMap { activity ->
                    (activity.selfEmotions + activity.stateEmotions).mapNotNull { emotion ->
                        val type = emotion.toType() ?: return@mapNotNull null
                        if (type.toEmotionGroup() != group) return@mapNotNull null
                        ActivityAggregate(activity.activity, type.toEmotionGroup(), emotion.emotionIntensity)
                    }
                }
            }
            .groupBy { it.activity to it.group }
            .entries
            .mapIndexed { index, entry ->
                ActivityEmotionSummaryResponse(
                    activityId = index + 1L,
                    activityContent = entry.key.first,
                    emotion = entry.key.second,
                    totalIntensity = entry.value.sumOf { it.intensity }.toLong(),
                    count = entry.value.size,
                    percentage = entry.value.size
                )
            }
            .sortedByDescending { it.totalIntensity }
    }

    fun dateSummary(memberId: String, group: EmotionGroup, period: Int): List<EmotionSummaryPeriodResponse> {
        val since = LocalDate.now().minusDays(period.toLong())
        return analysisEntries(memberId)
            .filter { (_, diary) -> !diary.writtenDate.isBefore(since) }
            .flatMap { (analysis, diary) ->
                allEmotionEntries(analysis).mapNotNull { emotion ->
                    if (emotion.type.toEmotionGroup() != group) return@mapNotNull null
                    diary.writtenDate to emotion.intensity
                }
            }
            .groupBy { it.first }
            .map { (date, values) ->
                EmotionSummaryPeriodResponse(
                    date = date,
                    emotionGroup = group,
                    intensity = values.sumOf { it.second }.toLong(),
                    count = values.size
                )
            }
            .sortedBy { it.date }
    }

    fun strength(memberId: String, year: Int? = null, month: Int? = null): StrengthResponse {
        val strengths = analysisEntries(memberId)
            .filter { (_, diary) ->
                year == null || month == null || (diary.writtenDate.year == year && diary.writtenDate.monthValue == month)
            }
            .flatMap { (analysis, _) -> analysis.activityAnalysis.mapNotNull { it.strength.takeIf(String::isNotBlank) } }

        val typeCount = strengths.groupingBy { it }.eachCount()
        return StrengthResponse(
            typeCount = typeCount,
            detailCount = typeCount.mapValues { mapOf(it.key to it.value) }
        )
    }

    private fun analysisEntries(memberId: String): List<Pair<DiaryAnalysisResponse, Diary>> =
        diaryRepository.findByAuthorId(memberId, cursor = null, limit = 1000)
            .map { normalizeAnalysis(it.metaData) to it }

    private fun normalizeAnalysis(metaData: Any): DiaryAnalysisResponse {
        if (metaData is DiaryAnalysisResponse) return metaData
        if (metaData is Map<*, *>) {
            val activities = metaData.listValue("activity_analysis", "activityAnalysis").mapNotNull(::activityFromMap)
            val reflectionMap = metaData.mapValue("reflection")
            return DiaryAnalysisResponse(
                activityAnalysis = activities,
                reflection = Reflection(
                    achievements = reflectionMap.stringList("achievements", "achievement"),
                    shortcomings = reflectionMap.stringList("shortcomings"),
                    todo = reflectionMap.stringList("todo", "todos"),
                    tomorrowMindSet = reflectionMap.stringValue("tomorrow_mindset", "tomorrowMindSet", "tomorrowMindset")
                )
            )
        }
        return DiaryAnalysisResponse(emptyList(), Reflection(emptyList(), emptyList(), emptyList()))
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

    private fun allEmotionEntries(analysis: DiaryAnalysisResponse): List<TypedEmotion> =
        analysis.activityAnalysis.flatMap { activity ->
            activity.selfEmotions + activity.stateEmotions + activity.peoples.flatMap { it.interactions }
        }.mapNotNull { emotion ->
            val type = emotion.toType() ?: return@mapNotNull null
            TypedEmotion(type = type, intensity = emotion.emotionIntensity)
        }

    private fun List<EmotionData>.toEmotionDetails(): List<EmotionDetailResponse> =
        groupBy { it.emotion }.map { (emotion, values) ->
            EmotionDetailResponse(
                emotion = emotion,
                count = values.size,
                intensity = values.map { it.emotionIntensity }.average().takeIf { !it.isNaN() } ?: 0.0,
                totalCount = values.size,
                totalIntensity = values.sumOf { it.emotionIntensity }
            )
        }

    private fun EmotionData.toType(): EmotionType? =
        EmotionType.entries.firstOrNull { it.name == emotion }

    private fun toDiaryResponse(diary: Diary, analysis: DiaryAnalysisResponse): DiaryResponse =
        DiaryResponse(
            SimpleDiaryInfoFields(
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
                emotions = allEmotionEntries(analysis).map { it.type.name }.distinct(),
                people = analysis.activityAnalysis.flatMap { it.peoples.map { person -> person.name } }.distinct(),
                targets = analysis.activityAnalysis.flatMap { it.peoples.map { person -> person.name } }.distinct()
            )
        )

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

    private data class TypedEmotion(val type: EmotionType, val intensity: Int)
    private data class PersonEmotionAggregate(val name: String, val group: EmotionGroup, val intensity: Int)
    private data class ActivityAggregate(val activity: String, val group: EmotionGroup, val intensity: Int)

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
