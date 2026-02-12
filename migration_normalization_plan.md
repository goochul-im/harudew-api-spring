# Diary metadata 정규화 마이그레이션 플랜

> **작성일**: 2026-02-12
> **대상**: `harudew-api-spring` (Kotlin + Spring Boot 3.5)
> **참조**: `Remotion-Server` (NestJS 원본)
> **목표**: `Diary.metaData: Any` 칼럼 완전 제거 → 모든 분석 데이터를 정규화된 엔티티에 raw 값으로 저장

---

## 목차

1. [현황 분석 — 코드 기반 증거](#1-현황-분석--코드-기반-증거)
2. [문제 정의](#2-문제-정의)
3. [변경 대상 데이터 매핑](#3-변경-대상-데이터-매핑)
4. [1단계: 새 엔티티/도메인 모델 추가](#4-1단계-새-엔티티도메인-모델-추가)
5. [2단계: DiaryService 분석 저장 로직 수정](#5-2단계-diaryservice-분석-저장-로직-수정)
6. [3단계: API 응답 재설계](#6-3단계-api-응답-재설계)
7. [4단계: Reflection 데이터 누락 수정](#7-4단계-reflection-데이터-누락-수정)
8. [5단계: DB 마이그레이션 스크립트](#8-5단계-db-마이그레이션-스크립트)
9. [6단계: 프론트엔드 수정 사항](#9-6단계-프론트엔드-수정-사항)
10. [수정 대상 파일 목록](#10-수정-대상-파일-목록)
11. [실행 순서 및 검증 방법](#11-실행-순서-및-검증-방법)

---

## 1. 현황 분석 — 코드 기반 증거

### 1-1. NestJS 원본에서 LLM이 반환하는 JSON 구조

`Remotion-Server/src/util/json.parser.ts`:

```typescript
interface DiaryAnalysis {
  activity_analysis: ActivityAnalysis[];
  reflection: Reflection;
}

interface ActivityAnalysis {
  activity: string;                    // "카페에서 공부했다"
  peoples: Person[];                   // 관련 인물들
  self_emotions: EmotionInteraction;   // {emotion: string[], emotion_intensity: number[]}
  state_emotions: EmotionInteraction;
  problem: ProblemAnalysis[];          // 문제/갈등 분석
  strength: string;                    // 강점 enum
}

interface Person {
  name: string;
  interactions: EmotionInteraction;    // 관계 감정
  name_intimacy: number;              // 호칭 친밀도 (0.2~1.0)
}

interface Reflection {
  achievements: string[];
  shortcomings: string[];              // ⚠️ Spring에서 엔티티 없음
  tomorrow_mindset: string;            // ⚠️ Spring에서 필드 자체 누락
  todo: string[];
}
```

### 1-2. Spring에서 현재 AI 분석 응답 모델

`harudew-api-spring/.../AiDiaryAnalysisResponse.kt`:

```kotlin
data class DiaryAnalysisResponse(
    val activityAnalysis: List<ActivityAnalysis>,
    val reflection: Reflection
)

data class Reflection(
    val achievements: List<String>,
    val shortcomings: List<String>,
    val todo: List<String>
    // ⚠️ tomorrow_mindset 필드 누락!
)
```

### 1-3. 현재 metadata 저장 경로

**NestJS** (`analysis-diary.service.ts:84`):
```typescript
diary.metadata = JSON.stringify(result);  // AI 원본 전체 저장
```

**Spring** (`DiaryService.kt:54`):
```kotlin
val result = Diary(
    ...
    metaData = analysisResult,  // DiaryAnalysisResponse 객체 통째로 저장
)
```

### 1-4. 현재 엔티티 저장과 metadata 중복 현황

NestJS `analysis-diary.service.ts:97-128`에서 metadata 저장 후 추가로 7개 서비스에 분배 저장:

| 분배 대상 | NestJS 코드 | Spring 엔티티 | 저장 방식 | 원본 보존? |
|-----------|-------------|--------------|----------|-----------|
| 인물 + 관계감정 | `targetService.createByDiary()` | `Person`, `DiaryPerson`, `EmotionPerson` | 누적 집계 | **NO** — 일기별 개별값 유실 |
| 활동 + 활동감정 | `activityService.createByDiary()` | `Activity`, `ActivityEmotion` | 누적 집계 | **NO** — `intensitySum/count`로 집계 |
| 상태 감정 | `emotionService.createDiaryStateEmotion()` | `DiaryEmotion` | 중복 시 합산 | **PARTIAL** — 동일 감정 합산 |
| 자아 감정 | `emotionService.createDiarySelfEmotion()` | `DiaryEmotion` | 중복 시 합산 | **PARTIAL** — 동일 감정 합산 |
| 회원 요약 | `memberSummaryService.updateSummary()` | `EmotionSummaryScore` | 날짜별 집계 | **NO** — 일기별 구분 불가 |
| 할 일 | `diaryTodoService.createByDiary()` | `DiaryTodo` | 개별 저장 | **YES** |
| 성취 | `achievementService.createByDiary()` | `DiaryAchievement` | 개별 저장 | **YES** |
| **문제/갈등** | **저장 안 함** | **엔티티 없음** | — | **NO** — metadata에만 존재 |
| **부족한 점** | **저장 안 함** | **엔티티 없음** | — | **NO** — metadata에만 존재 |
| **내일의 다짐** | **저장 안 함** | **엔티티 없음** | — | **NO** — metadata에만 존재 |

### 1-5. 현재 DiaryDetailResponse에서 metadata 사용

**NestJS** (`diary-detail.res.ts:80`):
```typescript
this.analysis = diary.metadata;  // JSON 그대로 프론트에 전달
```

**Spring** (`DiaryDetailResponse.kt:24`):
```kotlin
val analysis: JsonResponse  // metadata JSON을 JsonResponse로 매핑
```

`JsonResponse` 및 관련 클래스들 (`DiaryDetailResponse.kt:27-78`):
```kotlin
// 주석: "추후 리팩토링이 강력히 권장됩니다."
data class JsonResponse(val activityAnalysis: List<JsonActivityResponse>, val reflection: JsonReflectionResponse)
data class JsonReflectionResponse(val achievement, val shortcomings, val tomorrowMindset, val todos)
data class JsonActivityResponse(val activityName, val people, val selfEmotions, val stateEmotions, val problem, val strength)
data class JsonPeopleResponse(val name, val interactions, val nameIntensity)
data class JsonEmotionResponse(val emotion: List<String>, val intensity: List<Int>)
data class JsonProblemAnalysis(val situation, val approach, val outcome, val conflictResponseCode)
```

---

## 2. 문제 정의

### 핵심 결함 (코드 증거 기반)

| # | 문제 | 증거 | 영향 |
|---|------|------|------|
| 1 | **데이터 중복** | `DiaryService.kt:54`에서 `metaData = analysisResult`로 저장 + 이벤트로 엔티티 분배 저장 | 스토리지 낭비, 정합성 위험 |
| 2 | **집계로 인한 원본 유실** | `ActivityEmotion`은 `intensitySum/count` (`ActivityEmotion.kt:14`), `EmotionPerson`은 `intensity/count` (`EmotionPerson.kt:12`) | Activity별, Person별 개별 감정 raw 값 복원 불가 |
| 3 | **3개 필드 엔티티 부재** | `problem`, `shortcomings`, `tomorrow_mindset`에 대한 도메인 모델 없음 | API에서 metadata JSON에서만 조회 가능 |
| 4 | **타입 안정성 없음** | `Diary.kt:34`: `val metaData: Any` | 컴파일 타임 검증 불가, 런타임 오류 가능 |
| 5 | **tomorrow_mindset 누락** | `Reflection` 데이터 클래스에 필드 자체 없음 (`AiDiaryAnalysisResponse.kt:43-47`) | LLM이 반환해도 파싱 시 버려짐 |
| 6 | **DiaryEmotion 합산** | NestJS `emotion.service.ts`에서 동일 감정 중복 시 `intensity += e.intensity` | 같은 일기에서 2번 나타난 "기쁨"(5) + "기쁨"(3) = "기쁨"(8)로 합산, 원본 2건 복원 불가 |

---

## 3. 변경 대상 데이터 매핑

### metadata JSON의 모든 필드 → 정규화 후 저장 위치

```
metadata (DiaryAnalysisResponse)
├── activity_analysis[]
│   ├── activity            → Activity.content (기존)
│   ├── strength            → Activity.strength (기존)
│   ├── self_emotions[]
│   │   ├── emotion         → DiaryActivityEmotion.emotion (신규)
│   │   └── emotion_intensity → DiaryActivityEmotion.intensity (신규)
│   ├── state_emotions[]
│   │   ├── emotion         → DiaryActivityEmotion.emotion (신규)
│   │   └── emotion_intensity → DiaryActivityEmotion.intensity (신규)
│   ├── peoples[]
│   │   ├── name            → Person.name (기존)
│   │   ├── name_intimacy   → DiaryPersonEmotion.nameIntimacy (신규)
│   │   └── interactions[]
│   │       ├── emotion     → DiaryPersonEmotion.emotion (신규)
│   │       └── emotion_intensity → DiaryPersonEmotion.intensity (신규)
│   └── problem[]
│       ├── situation       → DiaryProblem.situation (신규)
│       ├── approach        → DiaryProblem.approach (신규)
│       ├── outcome         → DiaryProblem.outcome (신규)
│       └── conflict_response_code → DiaryProblem.conflictResponseCode (신규)
└── reflection
    ├── achievements[]      → DiaryAchievement.content (기존)
    ├── shortcomings[]      → DiaryReflection.shortcomings (신규)
    ├── tomorrow_mindset    → DiaryReflection.tomorrowMindset (신규)
    └── todo[]              → DiaryTodo.content (기존)
```

---

## 4. 1단계: 새 엔티티/도메인 모델 추가

### 4-1. `DiaryActivityEmotion` (신규) — Activity별 raw 감정 보존

현재 `ActivityEmotion`은 집계값(`intensitySum`, `count`)만 저장. Activity별 개별 감정 raw 값 보존 필요.

```kotlin
// emotion/domain/DiaryActivityEmotion.kt
class DiaryActivityEmotion(
    val id: Long? = null,
    val diary: Diary,
    val activity: Activity,
    val emotion: EmotionType,
    val emotionBase: EmotionBase,  // SELF 또는 STATE
    val intensity: Int             // raw 개별 강도값 (1-9)
)
```

**설계 근거**:
- NestJS `ActivityAnalysis`에서 `self_emotions`와 `state_emotions`는 각각 `{emotion: [], emotion_intensity: []}` 배열 쌍
- 현재 `ActivityEmotion`(`ActivityEmotion.kt:15-24`)은 `intensitySum: Double`, `count: Int`로 집계하여 개별값 복원 불가
- 이 엔티티는 **일기별, 활동별 개별 감정 스냅샷**을 보존

**파일 위치**: `src/main/kotlin/b1a4/harudew/emotion/domain/DiaryActivityEmotion.kt`

### 4-2. `DiaryPersonEmotion` (신규) — Person별 raw 감정 보존

현재 `EmotionPerson`(`EmotionPerson.kt:15-23`)은 날짜별 집계(`intensity: Float`, `count: Int`). 일기별 원본 per-person 감정 필요.

```kotlin
// person/domain/DiaryPersonEmotion.kt
class DiaryPersonEmotion(
    val id: Long? = null,
    val diary: Diary,
    val person: Person,
    val emotion: EmotionType,
    val intensity: Int,           // raw 개별 강도값 (1-9)
    val nameIntimacy: Float       // 원본 name_intimacy 보존 (0.2~1.0)
)
```

**설계 근거**:
- NestJS `PersonAnalysis`에서 `interactions: List<EmotionData>`는 개별 감정-강도 쌍
- `name_intimacy`는 호칭별 친밀도 (애칭 1.0 / 이름 0.5 / 성+직함 0.4 등) — 현재 엔티티에 저장 안 됨
- `DiaryPerson`(`DiaryPerson.kt:13-18`)은 `changeScore`만 가지고 있어 감정 상세 없음

**파일 위치**: `src/main/kotlin/b1a4/harudew/person/domain/DiaryPersonEmotion.kt`

### 4-3. `DiaryProblem` (신규) — 갈등/문제 분석

```kotlin
// diary/domain/model/DiaryProblem.kt
class DiaryProblem(
    val id: Long? = null,
    val diary: Diary,
    val activity: Activity,
    val situation: String,              // 갈등 상황 (14자 이하 명사구)
    val approach: String,               // 해결 접근 방식
    val outcome: String,                // 현재 상태/결과
    val conflictResponseCode: String    // enum: 회피형, 경쟁형, 타협형, 수용형, 협력형
)
```

**설계 근거**:
- NestJS `ProblemAnalysis`(`json.parser.ts:19-24`)는 4개 필드 구조
- 프롬프트(`prompt.constants.ts:133-142`)에서 5개 갈등 대응 코드 enum 정의
- 현재 Spring/NestJS 모두 problem을 별도 엔티티로 저장하지 않음 — metadata에만 존재

**파일 위치**: `src/main/kotlin/b1a4/harudew/diary/domain/model/DiaryProblem.kt`

### 4-4. `DiaryReflection` (신규) — 부족한 점 + 내일의 다짐

```kotlin
// diary/domain/model/DiaryReflection.kt
class DiaryReflection(
    val id: Long? = null,
    val diary: Diary,
    val shortcomings: List<String>,    // JSON 배열로 저장 또는 별도 테이블
    val tomorrowMindset: String?       // 내일의 다짐 텍스트
)
```

**설계 근거**:
- NestJS `Reflection`(`json.parser.ts:36-41`)에 `shortcomings: string[]`, `tomorrow_mindset: string`
- Spring `Reflection` 데이터 클래스(`AiDiaryAnalysisResponse.kt:43-47`)에 `tomorrow_mindset` **필드 자체 누락**
- `achievements`와 `todo`는 이미 `DiaryAchievement`, `DiaryTodo` 엔티티로 저장됨
- `shortcomings`와 `tomorrow_mindset`만 엔티티 부재

**파일 위치**: `src/main/kotlin/b1a4/harudew/diary/domain/model/DiaryReflection.kt`

### 4-5. `Diary` 도메인 모델 수정

**현재** (`Diary.kt:22-35`):
```kotlin
class Diary(
    ...
    val metaData: Any  // ← 제거 대상
)
```

**변경 후**:
```kotlin
class Diary(
    val id: Long? = null,
    val author: Member,
    val writtenDate: LocalDate,
    val content: String,
    val title: String = "diary title",
    val weather: String = "NONE",
    val photoPath: List<String>,
    val audioPath: List<String>,
    val isBookmark: Boolean = false,
    val latitude: Double?,
    val longitude: Double?,
    // metaData: Any  ← 삭제
)
```

---

## 5. 2단계: DiaryService 분석 저장 로직 수정

### 5-1. DiaryService 변경

**현재** (`DiaryService.kt:47-67`):
```kotlin
val result = Diary(
    ...
    metaData = analysisResult,  // ← 제거
)
val saveDiary = diaryRepository.save(result)
eventPublisher.publish(DiaryCreateEvent(
    saveDiary.id!!,
    command.content,
    analysisResult = analysisResult,  // 이벤트에는 유지
    ...
))
```

**변경 후**:
1. `Diary` 생성 시 `metaData` 파라미터 제거
2. `DiaryCreateEvent`에 `analysisResult`는 **그대로 전달** (이벤트 핸들러에서 분산 저장에 사용)

### 5-2. DiaryEventHandler 확장

**현재** (`DiaryEventHandler.kt:14-31`):
```kotlin
@Component
class DiaryEventHandler(
    private val diaryPreprocessingUseCase: DiaryPreprocessingUseCase
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun createHandler(event: DiaryCreateEvent) {
        diaryPreprocessingUseCase.ragPreprocessing(...)  // RAG 전처리만
    }
}
```

**변경 후**: 분석 결과를 새 엔티티들에 저장하는 로직 추가

```kotlin
@Component
class DiaryEventHandler(
    private val diaryPreprocessingUseCase: DiaryPreprocessingUseCase,
    // 새로 주입할 포트들
    private val diaryActivityEmotionPort: DiaryActivityEmotionCommandPort,
    private val diaryPersonEmotionPort: DiaryPersonEmotionCommandPort,
    private val diaryProblemPort: DiaryProblemCommandPort,
    private val diaryReflectionPort: DiaryReflectionCommandPort,
    // 기존 엔티티 저장 포트들 (NestJS에서 마이그레이션)
    private val activityCommandPort: ActivityCommandPort,
    private val personCommandPort: PersonCommandPort,
    private val emotionCommandPort: EmotionCommandPort,
    private val todoCommandPort: TodoCommandPort,
    private val achievementCommandPort: AchievementCommandPort,
    private val memberSummaryPort: MemberSummaryCommandPort,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun createHandler(event: DiaryCreateEvent) {
        val analysis = event.analysisResult

        // [기존] RAG 전처리
        diaryPreprocessingUseCase.ragPreprocessing(...)

        // [신규] Activity별 raw 감정 저장
        for (activity in analysis.activityAnalysis) {
            val savedActivity = activityCommandPort.save(activity, event.diaryId)

            // DiaryActivityEmotion 저장 (self + state)
            for (emotion in activity.selfEmotions) {
                diaryActivityEmotionPort.save(event.diaryId, savedActivity.id, emotion, EmotionBase.SELF)
            }
            for (emotion in activity.stateEmotions) {
                diaryActivityEmotionPort.save(event.diaryId, savedActivity.id, emotion, EmotionBase.STATE)
            }

            // DiaryProblem 저장
            for (problem in activity.problem) {
                diaryProblemPort.save(event.diaryId, savedActivity.id, problem)
            }

            // DiaryPersonEmotion 저장
            for (person in activity.peoples) {
                for (interaction in person.interactions) {
                    diaryPersonEmotionPort.save(event.diaryId, person.name, interaction, person.nameIntimacy)
                }
            }
        }

        // [신규] DiaryReflection 저장
        diaryReflectionPort.save(
            event.diaryId,
            analysis.reflection.shortcomings,
            analysis.reflection.tomorrowMindset
        )

        // [기존 마이그레이션] 집계 엔티티 저장
        emotionCommandPort.createDiaryEmotions(analysis, event.diaryId)
        todoCommandPort.createFromAnalysis(analysis.reflection.todo, event.diaryId)
        achievementCommandPort.createFromAnalysis(analysis.reflection.achievements, event.diaryId)
        memberSummaryPort.updateFromAnalysis(analysis, event.authorId)
    }
}
```

**주의**: 기존 집계 엔티티(`DiaryEmotion`, `ActivityEmotion`, `EmotionPerson`)는 **삭제하지 않는다**.
- 감정 통계/그래프 API가 집계 엔티티에 의존하고 있음
- 새 raw 엔티티는 추가하되, 집계 엔티티는 계속 생성
- 향후 집계 API를 raw 엔티티 기반 쿼리로 전환 시 집계 엔티티 폐기 가능

---

## 6. 3단계: API 응답 재설계

### 6-1. DiaryDetailResponse 변경

**현재** (`DiaryDetailResponse.kt:8-25`):
```kotlin
data class DiaryDetailResponse(
    ...
    val analysis: JsonResponse  // ← metadata JSON 직접 전달
)
```

**변경 후**:
```kotlin
data class DiaryDetailResponse(
    val id: Long,
    val writtenDate: LocalDate,
    val content: String,
    val photoPath: List<String>?,
    val audioPath: List<String>?,
    val isBookmarked: Boolean,
    val latitude: Float,
    val longitude: Float,
    val stressWarning: Boolean,
    val anxietyWarning: Boolean,
    val depressionWarning: Boolean,
    val recommendRoutine: RecommendRoutineResponse?,
    val beforeDiaryScores: DiaryEmotionScoreResponse,
    // ↓ 정규화된 분석 결과 (엔티티 기반)
    val activities: List<ActivityDetailResponse>,
    val people: List<PeopleAnalysisResponse>,
    val selfEmotions: List<EmotionAnalysisResponse>,
    val stateEmotions: List<EmotionAnalysisResponse>,
    val achievements: List<String>,
    val shortcomings: List<String>,
    val tomorrowMindset: String?,
    val todos: List<TodoAnalysisResponse>
)
```

### 6-2. ActivityDetailResponse (신규)

```kotlin
// diary/adapter/dto/response/ActivityDetailResponse.kt
data class ActivityDetailResponse(
    val id: Long,
    val name: String,
    val strength: String?,
    val selfEmotions: List<EmotionAnalysisResponse>,
    val stateEmotions: List<EmotionAnalysisResponse>,
    val people: List<ActivityPersonResponse>,
    val problems: List<ProblemResponse>
)

data class ActivityPersonResponse(
    val name: String,
    val nameIntimacy: Float,
    val emotions: List<EmotionAnalysisResponse>
)

data class ProblemResponse(
    val situation: String,
    val approach: String,
    val outcome: String,
    val conflictResponseCode: String
)
```

### 6-3. 삭제할 클래스들

`DiaryDetailResponse.kt` 내의 JSON 래퍼 클래스들 (모두 제거):
- `JsonResponse` (L34-38)
- `JsonReflectionResponse` (L40-47)
- `JsonActivityResponse` (L49-59)
- `JsonPeopleResponse` (L61-65)
- `JsonEmotionResponse` (L67-70)
- `JsonProblemAnalysis` (L72-78)

이 클래스들은 주석에서도 "추후 리팩토링이 강력히 권장됩니다"라고 명시되어 있음.

---

## 7. 4단계: Reflection 데이터 누락 수정

### Spring AiDiaryAnalysisResponse에 tomorrow_mindset 추가

**현재** (`AiDiaryAnalysisResponse.kt:43-47`):
```kotlin
data class Reflection(
    val achievements: List<String>,
    val shortcomings: List<String>,
    val todo: List<String>
    // tomorrow_mindset 누락!
)
```

**변경 후**:
```kotlin
data class Reflection(
    val achievements: List<String>,
    val shortcomings: List<String>,
    @JsonProperty("tomorrow_mindset")
    val tomorrowMindset: String? = null,
    val todo: List<String>
)
```

**원인**: NestJS 프롬프트(`prompt.constants.ts:114`)에서 LLM에게 `tomorrow_mindset` 필드를 요청하지만, Spring 파싱 클래스에서 필드를 누락시켜 Jackson 역직렬화 시 무시됨.

> 이 수정은 **다른 모든 변경보다 먼저** 적용해야 함. metadata에 저장되는 데이터가 현재 불완전하므로.

---

## 8. 5단계: DB 마이그레이션 스크립트

### 8-0. Flyway 도입

현재 프로젝트에 Flyway/Liquibase 미설정 (`build.gradle.kts`에 관련 의존성 없음).

**추가할 의존성** (`build.gradle.kts`):
```kotlin
implementation("org.flywaydb:flyway-core")
implementation("org.flywaydb:flyway-mysql")
```

**마이그레이션 디렉토리**: `src/main/resources/db/migration/`

### 8-1. V1__create_normalization_tables.sql — 새 테이블 생성

```sql
-- 1. diary_activity_emotion: Activity별 raw 감정
CREATE TABLE diary_activity_emotion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diary_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    emotion VARCHAR(20) NOT NULL,          -- EmotionType enum name
    emotion_base VARCHAR(10) NOT NULL,     -- SELF or STATE
    intensity INT NOT NULL,                -- 1-9
    CONSTRAINT fk_dae_diary FOREIGN KEY (diary_id) REFERENCES diary(id),
    CONSTRAINT fk_dae_activity FOREIGN KEY (activity_id) REFERENCES activity(id),
    INDEX idx_dae_diary (diary_id),
    INDEX idx_dae_activity (activity_id)
);

-- 2. diary_person_emotion: Person별 raw 감정
CREATE TABLE diary_person_emotion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diary_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,
    emotion VARCHAR(20) NOT NULL,
    intensity INT NOT NULL,
    name_intimacy FLOAT,
    CONSTRAINT fk_dpe_diary FOREIGN KEY (diary_id) REFERENCES diary(id),
    CONSTRAINT fk_dpe_person FOREIGN KEY (person_id) REFERENCES person(id),
    INDEX idx_dpe_diary (diary_id),
    INDEX idx_dpe_person (person_id)
);

-- 3. diary_problem: 갈등/문제 분석
CREATE TABLE diary_problem (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diary_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    situation VARCHAR(50) NOT NULL,
    approach VARCHAR(50) NOT NULL,
    outcome VARCHAR(50) NOT NULL,
    conflict_response_code VARCHAR(10) NOT NULL,
    CONSTRAINT fk_dp_diary FOREIGN KEY (diary_id) REFERENCES diary(id),
    CONSTRAINT fk_dp_activity FOREIGN KEY (activity_id) REFERENCES activity(id),
    INDEX idx_dp_diary (diary_id)
);

-- 4. diary_reflection: 부족한 점 + 내일의 다짐
CREATE TABLE diary_reflection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    diary_id BIGINT NOT NULL UNIQUE,       -- 1:1 관계
    shortcomings JSON,                      -- ["부족한 점1", "부족한 점2"]
    tomorrow_mindset TEXT,
    CONSTRAINT fk_dr_diary FOREIGN KEY (diary_id) REFERENCES diary(id)
);
```

### 8-2. V2__migrate_metadata_to_entities.sql — 데이터 마이그레이션

> **전제조건**: MySQL 8.0+ (JSON_TABLE 함수 필요)

```sql
-- ========================================
-- diary_reflection 마이그레이션
-- ========================================
INSERT INTO diary_reflection (diary_id, shortcomings, tomorrow_mindset)
SELECT
    d.id,
    JSON_EXTRACT(d.metadata, '$.reflection.shortcomings'),
    JSON_UNQUOTE(JSON_EXTRACT(d.metadata, '$.reflection.tomorrow_mindset'))
FROM diary d
WHERE d.metadata IS NOT NULL
  AND JSON_EXTRACT(d.metadata, '$.reflection') IS NOT NULL;

-- ========================================
-- diary_problem 마이그레이션
-- (activity별 problem 배열을 개별 행으로 풀기)
-- ========================================
INSERT INTO diary_problem (diary_id, activity_id, situation, approach, outcome, conflict_response_code)
SELECT
    d.id AS diary_id,
    a.id AS activity_id,
    JSON_UNQUOTE(p.situation) AS situation,
    JSON_UNQUOTE(p.approach) AS approach,
    JSON_UNQUOTE(p.outcome) AS outcome,
    JSON_UNQUOTE(p.conflict_response_code) AS conflict_response_code
FROM diary d
CROSS JOIN JSON_TABLE(
    d.metadata,
    '$.activity_analysis[*]' COLUMNS (
        activity_name VARCHAR(200) PATH '$.activity',
        NESTED PATH '$.problem[*]' COLUMNS (
            situation VARCHAR(50) PATH '$.situation',
            approach VARCHAR(50) PATH '$.approach',
            outcome VARCHAR(50) PATH '$.outcome',
            conflict_response_code VARCHAR(10) PATH '$.conflict_response_code'
        )
    )
) AS p
JOIN activity a ON a.diary_id = d.id AND a.content = p.activity_name
WHERE d.metadata IS NOT NULL
  AND p.situation IS NOT NULL
  AND p.situation != 'None';

-- ========================================
-- diary_activity_emotion 마이그레이션
-- (self_emotions + state_emotions 각각 풀기)
-- ========================================

-- Self emotions
INSERT INTO diary_activity_emotion (diary_id, activity_id, emotion, emotion_base, intensity)
SELECT
    d.id AS diary_id,
    a.id AS activity_id,
    JSON_UNQUOTE(JSON_EXTRACT(
        JSON_EXTRACT(d.metadata, CONCAT('$.activity_analysis[', aa.idx, '].self_emotions.emotion')),
        CONCAT('$[', e.idx, ']')
    )) AS emotion,
    'SELF' AS emotion_base,
    JSON_EXTRACT(
        JSON_EXTRACT(d.metadata, CONCAT('$.activity_analysis[', aa.idx, '].self_emotions.emotion_intensity')),
        CONCAT('$[', e.idx, ']')
    ) AS intensity
FROM diary d
-- 활동 인덱스 생성 (최대 20개 활동 가정)
CROSS JOIN (SELECT 0 AS idx UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
            UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) aa
-- 감정 인덱스 생성 (최대 10개 감정 가정)
CROSS JOIN (SELECT 0 AS idx UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4
            UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) e
JOIN activity a ON a.diary_id = d.id
    AND a.content = JSON_UNQUOTE(JSON_EXTRACT(d.metadata, CONCAT('$.activity_analysis[', aa.idx, '].activity')))
WHERE d.metadata IS NOT NULL
  AND JSON_EXTRACT(d.metadata, CONCAT('$.activity_analysis[', aa.idx, ']')) IS NOT NULL
  AND JSON_EXTRACT(
      JSON_EXTRACT(d.metadata, CONCAT('$.activity_analysis[', aa.idx, '].self_emotions.emotion')),
      CONCAT('$[', e.idx, ']')
  ) IS NOT NULL;

-- State emotions (동일 패턴, emotion_base = 'STATE')
-- ... (self_emotions와 동일 구조, 경로만 state_emotions로 변경)

-- ========================================
-- diary_person_emotion 마이그레이션
-- (peoples[].interactions 풀기)
-- ========================================
-- JSON_TABLE로 중첩 배열 접근
INSERT INTO diary_person_emotion (diary_id, person_id, emotion, intensity, name_intimacy)
SELECT
    d.id AS diary_id,
    p.id AS person_id,
    JSON_UNQUOTE(pe.emotion) AS emotion,
    pe.intensity,
    pe.name_intimacy
FROM diary d
CROSS JOIN JSON_TABLE(
    d.metadata,
    '$.activity_analysis[*].peoples[*]' COLUMNS (
        person_name VARCHAR(100) PATH '$.name',
        name_intimacy FLOAT PATH '$.name_intimacy',
        NESTED PATH '$.interactions.emotion[*]' COLUMNS (
            emotion_idx FOR ORDINALITY,
            emotion VARCHAR(20) PATH '$'
        )
    )
) AS pe
-- intensity는 별도 JSON_TABLE로 매칭 (동일 인덱스)
-- ... (구현 시 emotion과 emotion_intensity 배열 동기화 필요)
JOIN person p ON p.member_id = d.author_id AND p.name = pe.person_name
WHERE d.metadata IS NOT NULL;
```

> **참고**: 위 SQL은 개념적 가이드. 실제 구현 시 JSON 중첩 배열의 emotion/emotion_intensity 배열 동기화는 복잡하므로, **Kotlin 마이그레이션 스크립트**(Spring Boot ApplicationRunner)로 작성하는 것을 권장.

### 8-3. V3__drop_metadata_column.sql — metadata 칼럼 제거

```sql
-- 무결성 검증 후에만 실행
-- 검증: SELECT COUNT(*) FROM diary WHERE metadata IS NOT NULL
--       vs SELECT COUNT(DISTINCT diary_id) FROM diary_reflection
ALTER TABLE diary DROP COLUMN metadata;
```

### 대안: Kotlin 마이그레이션 스크립트

SQL로 중첩 JSON 배열을 풀기가 복잡하므로, Spring Boot `ApplicationRunner`로 구현하는 것을 **강력 권장**:

```kotlin
@Component
@Profile("migration")
class MetadataMigrationRunner(
    private val diaryRepository: DiaryRepository,
    private val objectMapper: ObjectMapper,
    // ... 새 엔티티 Repository들
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        val diaries = diaryRepository.findAllWithMetadata()
        for (diary in diaries) {
            val analysis = objectMapper.readValue(diary.metadata, DiaryAnalysisResponse::class.java)
            // ... 위 DiaryEventHandler 로직과 동일하게 엔티티 생성/저장
        }
    }
}
```

---

## 9. 6단계: 프론트엔드 수정 사항

### 접근 경로 변경 매핑

| 기존 프론트 접근 경로 | 새 접근 경로 |
|---|---|
| `diary.analysis` | (제거 — 하위 필드들이 diary 직속으로 이동) |
| `diary.analysis.activity_analysis` | `diary.activities` |
| `diary.analysis.activity_analysis[].activity` | `diary.activities[].name` |
| `diary.analysis.activity_analysis[].strength` | `diary.activities[].strength` |
| `diary.analysis.activity_analysis[].self_emotions.emotion[i]` | `diary.activities[].selfEmotions[i].emotionType` |
| `diary.analysis.activity_analysis[].self_emotions.emotion_intensity[i]` | `diary.activities[].selfEmotions[i].intensity` |
| `diary.analysis.activity_analysis[].state_emotions.emotion[i]` | `diary.activities[].stateEmotions[i].emotionType` |
| `diary.analysis.activity_analysis[].state_emotions.emotion_intensity[i]` | `diary.activities[].stateEmotions[i].intensity` |
| `diary.analysis.activity_analysis[].peoples[].name` | `diary.activities[].people[].name` |
| `diary.analysis.activity_analysis[].peoples[].interactions.emotion[i]` | `diary.activities[].people[].emotions[i].emotionType` |
| `diary.analysis.activity_analysis[].peoples[].interactions.emotion_intensity[i]` | `diary.activities[].people[].emotions[i].intensity` |
| `diary.analysis.activity_analysis[].peoples[].nameIntensity` | `diary.activities[].people[].nameIntimacy` |
| `diary.analysis.activity_analysis[].problem[].situation` | `diary.activities[].problems[].situation` |
| `diary.analysis.activity_analysis[].problem[].approach` | `diary.activities[].problems[].approach` |
| `diary.analysis.activity_analysis[].problem[].outcome` | `diary.activities[].problems[].outcome` |
| `diary.analysis.activity_analysis[].problem[].conflict_response_code` | `diary.activities[].problems[].conflictResponseCode` |
| `diary.analysis.reflection.achievement` | `diary.achievements` |
| `diary.analysis.reflection.shortcomings` | `diary.shortcomings` |
| `diary.analysis.reflection.tomorrow_mindset` | `diary.tomorrowMindset` |
| `diary.analysis.reflection.todo` | `diary.todos` |

### 주요 변경점

1. **감정 구조 변경**: `{emotion: [str], emotion_intensity: [int]}` 병렬 배열 → `[{emotionType, intensity}]` 객체 배열
2. **중첩 깊이 감소**: `diary.analysis.activity_analysis[].self_emotions.emotion[i]` (4단계) → `diary.activities[].selfEmotions[i]` (3단계)
3. **analysis 래퍼 제거**: 모든 분석 필드가 diary 루트 레벨로 이동

---

## 10. 수정 대상 파일 목록

### harudew-api-spring (백엔드)

| 파일 | 변경 유형 | 변경 내용 |
|---|---|---|
| `diary/domain/model/Diary.kt` | 수정 | `metaData: Any` 필드 제거 |
| `diary/domain/model/DiaryProblem.kt` | **신규** | 갈등/문제 도메인 모델 |
| `diary/domain/model/DiaryReflection.kt` | **신규** | 부족한 점 + 내일의 다짐 도메인 모델 |
| `emotion/domain/DiaryActivityEmotion.kt` | **신규** | Activity별 raw 감정 도메인 모델 |
| `person/domain/DiaryPersonEmotion.kt` | **신규** | Person별 raw 감정 도메인 모델 |
| `diary/application/port/out/analysis/AiDiaryAnalysisResponse.kt` | 수정 | `Reflection`에 `tomorrowMindset` 필드 추가 |
| `diary/adapter/dto/response/DiaryDetailResponse.kt` | **재설계** | `analysis: JsonResponse` 제거 → 정규화된 필드들로 교체, Json* 클래스 6개 삭제 |
| `diary/adapter/dto/response/ActivityDetailResponse.kt` | **신규** | Activity 상세 응답 + ProblemResponse + ActivityPersonResponse |
| `diary/application/service/DiaryService.kt` | 수정 | `metaData = analysisResult` 제거 |
| `global/orchestration/DiaryEventHandler.kt` | **대폭 확장** | 분석 결과 → 6개 새 엔티티 저장 로직 추가 |
| JPA Entity 클래스들 | **신규 4개** | DiaryActivityEmotionEntity, DiaryPersonEmotionEntity, DiaryProblemEntity, DiaryReflectionEntity |
| Repository 인터페이스 | **신규 4개** | 새 엔티티용 Spring Data JPA Repository |
| Out Port 인터페이스 | **신규 4개** | 새 엔티티용 Command Port |
| Repository 구현체 | **신규 4개** | Port → JPA Repository 위임 어댑터 |
| `build.gradle.kts` | 수정 | Flyway 의존성 추가 |
| `src/main/resources/db/migration/` | **신규** | V1, V2, V3 마이그레이션 스크립트 |
| `diary/adapter/dto/response/DiaryInfoFields.kt` | 수정 | `emotions: List<JsonEmotionResponse>?` 타입 변경 |

### 영향 받는 기존 파일 (간접)

| 파일 | 이유 |
|---|---|
| `diary/application/port/in/DiaryQueryUseCase.kt` | `findJsonById()` 반환 타입 변경 |
| 컨트롤러 (미구현이지만) | 응답 DTO 변경 반영 |
| 테스트 (`DiaryAnalysisAdapterTest.kt`) | `Reflection` 생성자에 `tomorrowMindset` 추가 |

---

## 11. 실행 순서 및 검증 방법

### 실행 순서

```
Phase 0: 즉시 수정 (데이터 손실 방지)
  └── Reflection에 tomorrowMindset 필드 추가 (AiDiaryAnalysisResponse.kt)

Phase 1: 새 엔티티 추가 (기존 코드 영향 없음)
  ├── DiaryActivityEmotion 도메인 + JPA Entity + Repository + Port
  ├── DiaryPersonEmotion 도메인 + JPA Entity + Repository + Port
  ├── DiaryProblem 도메인 + JPA Entity + Repository + Port
  └── DiaryReflection 도메인 + JPA Entity + Repository + Port

Phase 2: 저장 로직 추가 (이벤트 핸들러 확장)
  ├── DiaryEventHandler에 새 엔티티 저장 로직 추가
  └── (기존 metaData 저장은 유지 — 롤백 안전장치)

Phase 3: API 응답 전환
  ├── ActivityDetailResponse 등 새 DTO 생성
  ├── DiaryDetailResponse를 엔티티 기반으로 재설계
  └── Json* 래퍼 클래스들 삭제

Phase 4: 프론트엔드 수정
  └── API 응답 구조 변경에 맞춰 접근 경로 수정

Phase 5: DB 마이그레이션 (기존 데이터)
  ├── Kotlin 마이그레이션 스크립트로 기존 metadata → 새 테이블 INSERT
  └── 건수 검증

Phase 6: metaData 제거
  ├── Diary.metaData 필드 삭제
  ├── DiaryService에서 metaData 저장 제거
  └── DB 칼럼 DROP
```

### 검증 방법

| 검증 항목 | 방법 | 합격 기준 |
|-----------|------|----------|
| 빌드 | `./gradlew build` | 에러 0 |
| 단위 테스트 | 분석 결과 → 새 엔티티 저장 → 조회 | 원본 데이터 일치 |
| 마이그레이션 건수 | `SELECT COUNT(*) FROM diary WHERE metadata IS NOT NULL` vs `SELECT COUNT(DISTINCT diary_id) FROM diary_reflection` | 동일 |
| Problem 건수 | metadata 내 problem 배열 총 건수 vs `diary_problem` 행수 | 동일 |
| API 응답 | 새 DiaryDetailResponse가 기존 analysis JSON의 모든 정보를 포함 | 필드 누락 0 |
| 감정 원본 보존 | 특정 일기의 activity별 감정 조회 → metadata 원본과 비교 | raw 값 일치 |
| 통합 테스트 | 일기 생성 → AI 분석 → 엔티티 저장 → 조회 전체 플로우 | E2E 성공 |

### 롤백 전략

- **Phase 2까지**: metadata 저장 유지 → 새 엔티티만 추가 삭제하면 롤백 완료
- **Phase 3 이후**: API 응답 변경 + 프론트 수정이 동시에 배포되어야 함 → 버전 분기 배포 권장
- **Phase 6**: metadata 칼럼 DROP 전 반드시 백업. DROP 후 롤백 불가

---

## 부록: 기존 집계 엔티티와의 관계

### 집계 엔티티 유지 이유

| 기존 엔티티 | 역할 | raw 엔티티와의 관계 |
|-------------|------|-------------------|
| `DiaryEmotion` | 일기별 감정 (통계 API) | `DiaryActivityEmotion`에서 집계 가능하나, 기존 API 호환을 위해 유지 |
| `ActivityEmotion` | 활동별 감정 누적 (관계 분석) | `DiaryActivityEmotion`에서 `GROUP BY activity` 집계 가능 |
| `EmotionPerson` | 인물별 감정 누적 (관계 그래프) | `DiaryPersonEmotion`에서 `GROUP BY person` 집계 가능 |
| `EmotionSummaryScore` | 회원 날짜별 감정 요약 (기간 통계) | 유지 (성능상 사전 집계 필요) |

**향후 계획**: Phase 6 완료 후, 집계 엔티티를 raw 엔티티 기반 쿼리로 대체하는 리팩토링 별도 진행 가능.
