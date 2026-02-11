# Remotion-Server 전체 기능 분석 및 Kotlin-Spring 마이그레이션 가이드

> **목적**: NestJS(Remotion-Server) → Kotlin-Spring(harudew-api-spring) 헥사고날 아키텍처 마이그레이션을 위한 상세 기능 분석서
>
> **대상 프로젝트**:
> - 원본: `Remotion-Server` (NestJS + TypeORM + MySQL + Qdrant)
> - 마이그레이션: `harudew-api-spring` (Kotlin + Spring Boot + JPA + Qdrant)

---

## 목차

1. [마이그레이션 현황 요약](#1-마이그레이션-현황-요약)
2. [일기 생성 → AI 분석 파이프라인 (핵심)](#2-일기-생성--ai-분석-파이프라인-핵심)
3. [감정 시스템 (Emotion)](#3-감정-시스템-emotion)
4. [인물 시스템 (Target)](#4-인물-시스템-target)
5. [활동 시스템 (Activity)](#5-활동-시스템-activity)
6. [벡터 검색 / RAG 파이프라인](#6-벡터-검색--rag-파이프라인)
7. [키워드 검색](#7-키워드-검색)
8. [관계 분석 (Relation)](#8-관계-분석-relation)
9. [회원 요약 / 캐릭터 (MemberSummary)](#9-회원-요약--캐릭터-membersummary)
10. [루틴 추천 (Routine)](#10-루틴-추천-routine)
11. [컨텐츠 추천 (Recommend)](#11-컨텐츠-추천-recommend)
12. [할 일 관리 (Todo / DiaryTodo)](#12-할-일-관리-todo--diarytodo)
13. [알림 / 웹푸시 (Notification / WebPush)](#13-알림--웹푸시-notification--webpush)
14. [파일 업로드 (Upload / S3)](#14-파일-업로드-upload--s3)
15. [성취 클러스터링 (Achievement)](#15-성취-클러스터링-achievement)
16. [활동 클러스터링 (ActivityCluster)](#16-활동-클러스터링-activitycluster)
17. [강점 분석 (Strength)](#17-강점-분석-strength)
18. [지도 (Map)](#18-지도-map)
19. [YouTube 추천](#19-youtube-추천)
20. [성능 최적화 마이그레이션 전략](#20-성능-최적화-마이그레이션-전략)

---

## 1. 마이그레이션 현황 요약

### Spring에서 완료된 부분

| 영역 | 상태 | 비고 |
|------|------|------|
| 헥사고날 아키텍처 구조 | ✅ 완료 | Port/Adapter 패턴 적용 |
| 인증 (OAuth2 + JWT) | ✅ 완료 | Google, Kakao, SuperUser |
| 일기 생성 (create) | ✅ 완료 | 병렬 업로드 + AI 분석 |
| AI 분석 어댑터 (Bedrock) | ✅ 완료 | `DiaryAnalysisAdapter` |
| RAG 전처리 (tagging + chunking + embedding) | ✅ 완료 | `DiaryPreprocessingService` |
| 키워드 전처리 (extraction + embedding) | ✅ 완료 | `DiaryPreprocessingService` |
| Qdrant 인프라 (sentence + keyword 컬렉션) | ✅ 완료 | `QdrantSentenceAdapter`, `QdrantKeywordVectorAdapter` |
| 도메인 이벤트 시스템 | ✅ 완료 | `DiaryCreateEvent` + `DiaryEventHandler` |
| 도메인 모델 (Diary, Member, Emotion 등) | ✅ 완료 | 순수 Kotlin 클래스 |
| 글로벌 인프라 (AI, Embed, S3, Crypto) | ✅ 완료 | `global/infrastructure/` |

### Spring에서 아직 필요한 부분

| 영역 | 우선순위 | NestJS 복잡도 | 비고 |
|------|----------|-------------|------|
| **일기 CRUD (조회/수정/삭제)** | 🔴 높음 | 중간 | DiaryController 비어있음 |
| **분석 결과 → 엔티티 분해 저장** | 🔴 높음 | 높음 | Activity, Target, DiaryEmotion 등 생성 로직 |
| **RAG 검색 (3단계 파이프라인)** | 🔴 높음 | 높음 | Vector → Rerank → LLM 검증 |
| **인물(Target) CRUD + 친밀도 계산** | 🟡 중간 | 중간 | closenessScore, affection |
| **관계 분석 그래프** | 🟡 중간 | 중간 | RelationService |
| **회원 요약 + 캐릭터** | 🟡 중간 | 중간 | MemberSummary, EmotionSummaryScore |
| **활동 클러스터링** | 🟡 중간 | 높음 | Vector + RDB 이중 저장 |
| **성취 클러스터링** | 🟡 중간 | 높음 | Vector + RDB 이중 저장 |
| **할 일 관리 (Todo + TodoCalendar)** | 🟡 중간 | 낮음 | 단순 CRUD + Cron |
| **루틴 추천** | 🟡 중간 | 중간 | LLM 기반 추출 |
| **컨텐츠 추천 (Recommend)** | 🟢 낮음 | 높음 | 7개 모듈 조합, LLM + YouTube |
| **알림 / 웹푸시** | 🟢 낮음 | 중간 | Web Push API |
| **강점 분석** | 🟢 낮음 | 낮음 | 단순 집계 |
| **지도 기능** | 🟢 낮음 | 낮음 | 좌표 기반 조회 |
| **YouTube API** | 🟢 낮음 | 낮음 | Cron + API |

---

## 2. 일기 생성 → AI 분석 파이프라인 (핵심)

### 전체 흐름도

```
[HTTP 요청] POST /diary (multipart: content, photos, audio)
     │
     ▼
DiaryController.create()
     │  S3에 사진/오디오 업로드
     ▼
DiaryService.createDiary(memberId, dto, imageUrl, audioUrl)
     │
     ├──[병렬 1] analysisDiaryService.analysisAndSaveDiary()  ─── 메인 분석
     ├──[병렬 2] analysisDiaryService.analysisAndSaveDiaryRoutine()  ─── 루틴 추출
     └──[비동기]  analysisDiaryService.getTaggingContent()  ─── 태깅 (fire-and-forget)
     │
     ▼  (병렬 완료 후)
     ├── sentenceParserService.createByDiary(diary, taggedContent)  ─── 벡터 저장
     ├── notificationService.createRoutineNotification()  ─── 루틴 알림
     ├── memberCharacterService.calculateMemberCharacter()  ─── 캐릭터 재계산
     └── return diary.id
```

### 2-1. DiaryController.create() — HTTP 진입점

```typescript
// src/diary/diary.controller.ts
@Post()
@UseGuards(AuthGuard('jwt'))
@UseInterceptors(FilesInterceptor('photo', 10))
async create(
  @CurrentUser() user,
  @Body() body: CreateDiaryDto,
  @UploadedFiles() files: { photo?: Express.Multer.File[], audios?: Express.Multer.File[] }
) {
  // 1. 환경 가드 (특정 사용자 차단)
  if (process.env.MAKE_ENV === 'NO' && ['lee','anne'].includes(user.id))
    throw new ForbiddenException();

  // 2. S3 업로드
  let imageUrl = files?.photo ? await this.s3Service.uploadMultipleFiles(files.photo) : undefined;
  let audioUrl = files?.audios ? (await this.uploadService.uploadAudiosToS3(files.audios)).urls[0] : undefined;

  // 3. 일기 생성 파이프라인 시작
  const diaryId = await this.diaryService.createDiary(user.id, body, imageUrl, audioUrl);
  return new CreateDiaryRes(diaryId);
}
```

**헥사고날 매핑**: `DiaryController` → Inbound Adapter (`adapter/in/web/`)

### 2-2. DiaryService.createDiary() — 오케스트레이터

```typescript
// src/diary/diary.service.ts
async createDiary(memberId: string, dto: CreateDiaryDto, imageUrl?: string[], audioUrl?: string) {
  // 1. 태깅 먼저 시작 (fire-and-forget)
  const taggingPromise = this.analysisDiaryService.getTaggingContent(dto.content);

  // 2. 메인 분석 + 루틴 추출 병렬 실행
  const [result, routine] = await Promise.all([
    this.analysisDiaryService.analysisAndSaveDiary(memberId, dto, imageUrl, audioUrl),
    this.analysisDiaryService.analysisAndSaveDiaryRoutine(memberId, dto.content),
  ]);

  // 3. 태깅 완료 시 벡터 DB에 문장 저장
  taggingPromise
    .then((tagging) => this.sentenceParserService.createByDiary(result, tagging))
    .catch((e) => this.logger.error(`Tagging error: ${e.message}`));

  // 4. 루틴 생성 시 알림
  if (routine) await this.notificationService.createRoutineNotification(memberId);

  // 5. 캐릭터 재계산
  let newCharacter = await this.memberCharacterService.calculateMemberCharacter(memberId);
  await this.memberService.saveCharacter(memberId, newCharacter);

  return result.id;
}
```

**현재 Spring 상태**: `DiaryService.create()`에서 분석 + 사진 업로드를 코루틴으로 병렬 실행 후 `DiaryCreateEvent`를 발행. 그러나 **분석 결과를 개별 엔티티로 분해 저장하는 로직이 아직 없음** — `metaData`에 JSON으로만 저장.

**헥사고날 매핑**: `DiaryService` → Application Service (`application/service/`)

### 2-3. AnalysisDiaryService.analysisAndSaveDiary() — 핵심 분석 파이프라인

이 함수가 **전체 시스템에서 가장 중요한 함수**. 1번의 LLM 호출로 얻은 결과를 7개 서비스에 분배 저장.

```typescript
// src/analysis/analysis-diary.service.ts
async analysisAndSaveDiary(memberId: string, dto: CreateDiaryDto, imageUrl?, audioUrl?) {
  // ━━━━ STEP 1: LLM 호출 (Claude 3.5 Sonnet) ━━━━
  const result: DiaryAnalysis = await this.promptService.serializeAnalysis(dto.content);

  // ━━━━ STEP 2: 감정 유효성 검증 ━━━━
  this.filterInvalidEmotionsFromResult(result);
  // → EmotionType enum에 없는 감정 제거
  // → '안타까움'/'걱정' → '유대'로 매핑

  // ━━━━ STEP 3: 회원 조회 ━━━━
  const author = await this.memberService.findOne(memberId);

  // ━━━━ STEP 4: 일기 엔티티 저장 ━━━━
  const diary = new Diary();
  diary.author = author;
  diary.content = this.cryptoService.encrypt(dto.content);  // AES 암호화
  diary.metadata = JSON.stringify(result);                    // AI 원본 저장
  diary.written_date = dto.writtenDate;
  diary.title = 'demo';  // ⚠️ 하드코딩 (querySummary 미사용)
  const saveDiary = await this.diaryRepository.save(diary);

  // ━━━━ STEP 5: 키워드 추출 (비동기) ━━━━
  this.keywordService.createByDiary(saveDiary, dto.content).catch(e => console.error(e));

  // ━━━━ STEP 6: 분석 결과에서 데이터 추출 ━━━━
  const allPeopleInDiary = result.activity_analysis.flatMap(a => a.peoples);
  const selfEmotions = result.activity_analysis.flatMap(a =>
    this.util.toCombinedEmotionTyped(a.self_emotions));
  const stateEmotions = result.activity_analysis.flatMap(a =>
    this.util.toCombinedEmotionTyped(a.state_emotions));

  // ━━━━ STEP 7: 7개 서비스에 분배 저장 (순차 실행) ━━━━

  // [저장 1] 인물 + 관계감정 + DiaryTarget
  await this.targetService.createByDiary(allPeopleInDiary, saveDiary, author);

  // [저장 2] 활동 + 활동감정 + 활동클러스터 + ActivityTarget
  await this.activityService.createByDiary(result.activity_analysis, saveDiary);

  // [저장 3] 상태 감정 (DiaryEmotion with EmotionBase.State)
  await this.emotionService.createDiaryStateEmotion(stateEmotions, saveDiary);

  // [저장 4] 자아 감정 (DiaryEmotion with EmotionBase.Self)
  await this.emotionService.createDiarySelfEmotion(selfEmotions, saveDiary);

  // [저장 5] 회원 요약 (MemberSummary + EmotionSummaryScore)
  await this.memberSummaryService.updateSummaryFromDiary(
    allPeopleInDiary, selfEmotions, stateEmotions, author, dto.writtenDate);

  // [저장 6] 할 일 (DiaryTodo)
  await this.diaryTodoService.createByDiary(result.reflection.todo, saveDiary, author);

  // [저장 7] 성취 (DiaryAchievement + 클러스터)
  await this.achievementService.createByDiary(result.reflection.achievements, saveDiary, author);

  return saveDiary;
}
```

### 2-4. ClaudeService — LLM 게이트웨이

```typescript
// src/claude/claude.service.ts

// ━━━━ 일기 분석 (메인) ━━━━
async serializeAnalysis(prompt: string): Promise<DiaryAnalysis> {
  return this.queryDiaryPatterns(prompt);
}

async queryDiaryPatterns(prompt: string): Promise<DiaryAnalysis> {
  const fullPrompt = this.patternAnalysisPrompt(prompt);  // PROMPT_ANALYZE 래핑
  const response = await this.getResponseToSonnet3(fullPrompt);
  // → Claude 3.5 Sonnet, temp=0, topP=0.9, topK=10
  const cleaned = this._cleanJsonResponse(response);       // 마크다운 제거
  const parsed = JSON.parse(cleaned);
  this._applyPsychologicalDistance(parsed);                 // 심리적 거리 계산
  return parsed;
}

// ━━━━ 루틴 추출 ━━━━
async serializeRoutine(prompt: string): Promise<EmotionLevels> {
  const fullPrompt = this.ActionAnalysis(prompt);  // PROMPT_ROUTINE 래핑
  const response = await this.getResponseToSonnet4(fullPrompt);
  // → Claude Sonnet 4, temp=0.05
  return JSON.parse(this._cleanJsonResponse(response));
}

// ━━━━ 태깅 ━━━━
async getTaggingDiary(content: string): Promise<string> {
  const prompt = taggingPrompt(content);
  return this.getResponseToSonnet4(prompt);
  // → "[여행, 기쁨] 제주도에 갔다\n[가족] 엄마와 함께..." 형태 반환
}

// ━━━━ 키워드 추출 ━━━━
async getParsingKeywordDiary(content: string): Promise<string[]> {
  const prompt = parsingKeywordPrompt(content);
  const response = await this.getResponseToSonnet4(prompt);
  return JSON.parse(response);  // ["여행", "제주도", "엄마"] 형태
}

// ━━━━ RAG 검증 ━━━━
async getSearchDiary(query: string, documents: any[]): Promise<SimilarSentence[]> {
  const prompt = promptRAG(query, documents, LocalDate.now());
  const response = await this.getResponseToSonnet4(prompt);
  return JSON.parse(response);  // [{diary_id, sentence, is_similar: boolean}]
}

// ━━━━ 추천 코멘트 ━━━━
async getRecommendComment(activities, emotion, dayOfWeek): Promise<string> {
  // → Amazon Nova Lite, temp=1.0
}

// ━━━━ Bedrock 래퍼 ━━━━
private async getResponseToSonnet4(prompt): Promise<string> {
  // model: 'apac.anthropic.claude-sonnet-4-20250514-v1:0', temp: 0.05, maxTokens: 4000
}
private async getResponseToSonnet3(prompt): Promise<string> {
  // model: 'apac.anthropic.claude-3-5-sonnet-20241022-v2:0', temp: 0, topP: 0.9, topK: 10
}
private async getResponseToNovaLite(prompt, temp, topP): Promise<string> {
  // model: 'apac.amazon.nova-lite-v1:0'
}
```

**현재 Spring 상태**: `BedrockClientProvider`에서 Spring AI `ChatClient` 사용. `DiaryAnalysisAdapter`에서 분석 호출 완료. 그러나 **태깅, 키워드 추출, RAG 검증, 루틴 추출, 추천 코멘트용 LLM 호출은 각각 별도 포트/어댑터 필요**.

### 2-5. DiaryAnalysis 응답 구조 (LLM이 반환하는 JSON)

```typescript
// src/util/json.parser.ts
interface DiaryAnalysis {
  activity_analysis: ActivityAnalysis[];
  reflection: {
    todo: string[];            // AI가 추출한 할 일 목록
    achievements: string[];     // AI가 추출한 성취 목록
  };
}

interface ActivityAnalysis {
  activity: string;                    // "카페에서 공부했다"
  peoples: Person[];                   // 관련 인물들
  problems: Problem[];                 // 문제/고민
  self_emotions: EmotionInteraction;   // 자아 감정 {emotion: string[], emotion_intensity: number[]}
  state_emotions: EmotionInteraction;  // 상태 감정
  strength: string | null;             // 강점 ("집중력")
  weakness: string | null;             // 약점 ("인내심 부족")
}

interface Person {
  name: string;               // "민수"
  relation: string;           // "친구"
  interactions: EmotionInteraction; // 이 사람에 대한 감정
  psychological_distance?: number;  // 심리적 거리 (후처리로 계산)
}
```

**헥사고날 매핑**: `DiaryAnalysis` → Application Port의 `AiDiaryAnalysisResponse`에 해당

---

## 3. 감정 시스템 (Emotion)

### 3단계 감정 분류 체계

```
EmotionBase (최상위)
├── RELATION (관계 감정) — 22개: 감사, 분노, 짜증, 신뢰, 배신감, 질투, 애정, 연민...
├── SELF (자아 감정) — 10개: 자신감, 성취감, 후회, 죄책감, 열등감...
└── STATE (상태 감정) — 28개: 행복, 기쁨, 우울, 불안, 피로, 평온, 흥분...

EmotionGroup (6개 그룹)
├── 활력 (Vitality)  — 긍정
├── 안정 (Stability) — 긍정
├── 유대 (Bond)      — 긍정
├── 스트레스 (Stress) — 부정
├── 불안 (Anxiety)    — 부정
└── 우울 (Depression) — 부정
```

### 감정 이중 저장 구조

**DiaryEmotion** — 일기별 감정 기록 (스냅샷)
```typescript
// src/emotion/emotion.service.ts
async createDiaryEmotionByBase(emotions: CombinedEmotion[], diary: Diary, base: EmotionBase) {
  for (const e of emotions) {
    const existing = await this.diaryEmotionRepository.findOne({
      where: { diary: { id: diary.id }, emotion: e.emotion }
    });
    if (existing) {
      existing.intensity += e.intensity;
      await this.diaryEmotionRepository.save(existing);
    } else {
      const entity = new DiaryEmotion();
      entity.diary = diary;
      entity.emotion = e.emotion;        // EmotionType
      entity.emotionBase = base;          // Relation/Self/State
      entity.intensity = e.intensity;     // 1~9
      await this.diaryEmotionRepository.save(entity);
    }
  }
}
```

**EmotionTarget** — 인물별 누적 감정 (관계 감정만)
```typescript
async createOrUpdateEmotionTarget(target: Target, emotions: CombinedEmotion[], date: LocalDate) {
  for (const e of emotions) {
    const existing = await this.emotionTargetRepository.findOne({
      where: { target: { id: target.id }, emotion: e.emotion }
    });
    if (existing) {
      existing.emotion_intensity += e.intensity;  // 누적!
      existing.count += 1;
      existing.feel_date = date;
      await this.emotionTargetRepository.save(existing);
    } else {
      const entity = new EmotionTarget();
      entity.target = target;
      entity.emotion = e.emotion;
      entity.emotion_intensity = e.intensity;
      entity.count = 1;
      entity.feel_date = date;
      await this.emotionTargetRepository.save(entity);
    }
  }
}
```

### 감정 분석 API (EmotionController)

```typescript
// src/emotion/emotion.controller.ts — 9개 엔드포인트
GET /emotion/period?startDate=&endDate=         // 기간별 감정 변화 (날짜별 EmotionGroup 집계)
GET /emotion/weekday                             // 요일별 감정 패턴
GET /emotion/score/:diaryId                      // 일기의 감정 점수
GET /emotion/target/:targetId                    // 인물별 감정 요약
GET /emotion/target/positive/detail/:id          // 인물별 긍정 감정 상세
GET /emotion/target/negative/detail/:id          // 인물별 부정 감정 상세
GET /emotion/activity/positive/:emotionGroup     // 긍정 활동별 감정
GET /emotion/activity/negative/:emotionGroup     // 부정 활동별 감정
GET /emotion/base                                // EmotionBase별 전체 분석
```

**핵심 조회 메서드들:**

```typescript
// 기간별 감정 집계 — MemberSummary + EmotionSummaryScore에서 조회
async getAllEmotionsGroupedByDateRange(memberId, startDate, endDate) {
  return this.emotionSummaryScoreRepo.createQueryBuilder('score')
    .innerJoin('score.summary', 'summary')
    .where('summary.member.id = :memberId', { memberId })
    .andWhere('summary.date BETWEEN :start AND :end', { start: startDate, end: endDate })
    .select(['score.emotion AS emotionGroup', 'summary.date AS date',
             'SUM(score.score) AS totalScore', 'SUM(score.count) AS totalCount'])
    .groupBy('score.emotion, summary.date')
    .getRawMany();
}

// 일기의 감정 점수 계산
async getEmotionScoreAndGroup(diaryId: number) {
  // DiaryEmotion 전체 조회 → EmotionGroup별 합산 → 대표 감정 그룹 결정
  const emotions = await this.diaryEmotionRepository.find({ where: { diary: { id: diaryId } } });
  const groupScores = {};
  for (const e of emotions) {
    const group = getEmotionGroup(e.emotion);
    groupScores[group] = (groupScores[group] || 0) + e.intensity;
  }
  // 가장 높은 EmotionGroup을 대표 감정으로 선정
  return { scores: groupScores, representative: maxGroup };
}
```

**헥사고날 매핑 제안**:
- `EmotionCommandPort` (Out) — 감정 엔티티 생성/수정
- `EmotionQueryPort` (Out) — 감정 집계 조회
- `EmotionAnalysisUseCase` (In) — 기간별/요일별/인물별 감정 분석

---

## 4. 인물 시스템 (Target)

### 인물 생성 및 친밀도 계산

```typescript
// src/target/target.service.ts
async createByDiary(peoples: Person[], diary: Diary, member: Member) {
  const allRelationEmotions: CombinedEmotion[] = [];

  for (const person of peoples) {
    if (!person.name || person.name === '없음') continue;

    // 1. 기존 인물 찾기 or 새로 생성
    let target = await this.targetRepo.findOne({
      where: { member: { id: member.id }, name: person.name }
    });

    // 2. 친밀도 계산
    const closenessChange = this.calculateClosenessScore(person.interactions);
    const affectionChange = this.calculateAffection(person.interactions);

    if (target) {
      target.count += 1;
      target.closenessScore = Math.min(90, Math.max(0, target.closenessScore + closenessChange));
      target.affection += affectionChange;
      target.recent_date = diary.written_date;
    } else {
      target = new Target();
      target.name = person.name;
      target.member = member;
      target.closenessScore = 30 + closenessChange;  // 기본값 30
      target.affection = affectionChange;
      target.count = 1;
      target.recent_date = diary.written_date;
      target.relation = person.relation;
    }
    await this.targetRepo.save(target);

    // 3. DiaryTarget 연결
    await this.createDiaryTarget(target, diary, closenessChange);

    // 4. EmotionTarget 누적
    const emotions = this.util.toCombinedEmotionTyped(person.interactions);
    await this.emotionService.createOrUpdateEmotionTarget(target, emotions, diary.written_date);

    allRelationEmotions.push(...emotions);
  }

  // 5. 관계 감정을 DiaryEmotion으로도 저장
  await this.emotionService.createDiaryEmotionForTarget(allRelationEmotions, diary);
}
```

### 친밀도 점수 계산 로직

```typescript
// 감정 강도에 따른 가중치 (긍정/부정에 따라 부호 다름)
private calculateClosenessScore(interactions: EmotionInteraction): number {
  let score = 0;
  for (let i = 0; i < interactions.emotion.length; i++) {
    const emotion = interactions.emotion[i];
    const intensity = interactions.emotion_intensity[i];
    const isPositive = positiveRelationEmotions.includes(emotion);
    // 강도별 가중치: 1-3 → ÷3, 4-6 → ÷2, 7-9 → ÷1
    const weight = intensity <= 3 ? intensity/3 : intensity <= 6 ? intensity/2 : intensity;
    score += isPositive ? weight : -weight;
  }
  return score;
}
```

**헥사고날 매핑 제안**:
- `PersonCommandPort` (Out) — Target CRUD
- `PersonQueryPort` (Out) — Target 조회
- `DiaryPersonLinkPort` (Out) — DiaryTarget 생성

---

## 5. 활동 시스템 (Activity)

### 활동 생성 + 벡터 임베딩 + 클러스터링

```typescript
// src/activity/activity.service.ts
async createByDiary(activities: ActivityAnalysis[], diary: Diary) {
  for (const act of activities) {
    // 1. 활동 엔티티 생성 + SimCSE 임베딩
    const activity = new Activity();
    activity.content = act.activity;
    activity.diary = diary;
    activity.date = diary.written_date;
    activity.vector = await this.embedder.embed(act.activity);  // 768-dim SimCSE
    activity.strength = act.strength;
    activity.weakness = act.weakness;
    const saved = await this.repo.save(activity);

    // 2. 활동 클러스터링 (벡터 유사도 기반)
    await this.activityClusterService.createByActivity(saved, diary, diary.author);

    // 3. 활동 감정 저장
    const selfEmotions = this.util.toCombinedEmotionTyped(act.self_emotions);
    const stateEmotions = this.util.toCombinedEmotionTyped(act.state_emotions);
    for (const e of selfEmotions) {
      await this.saveOrUpdateActivityEmotion(e, saved, EmotionBase.State);
      // ⚠️ 버그: self 감정인데 EmotionBase.State로 저장함
    }
    for (const e of stateEmotions) {
      await this.saveOrUpdateActivityEmotion(e, saved, EmotionBase.State);
    }

    // 4. ActivityTarget 연결 (인물 ↔ 활동)
    for (const person of act.peoples) {
      const target = await this.targetRepo.findOne({
        where: { name: person.name, member: { id: diary.author.id } }
      });
      if (target) {
        const at = new ActivityTarget();
        at.activity = saved;
        at.target = target;
        await this.activityTargetRepo.save(at);
      }
    }
  }
}
```

**헥사고날 매핑 제안**:
- `ActivityCommandPort` (Out) — Activity CRUD
- `ActivityEmbedderPort` (Out) — SimCSE 임베딩
- `ActivityClusterPort` (Out) — 클러스터링

---

## 6. 벡터 검색 / RAG 파이프라인

### 6-1. 문장 저장 (일기 생성 시)

```typescript
// src/sentence-parser/sentence-parser.service.ts
async createByDiary(diary: Diary, taggingContent: string) {
  // 1. 태그된 텍스트를 문장으로 분리 (외부 파서 모델)
  const sentences = await this.parsingText(taggingContent);
  // → POST PARSER_MODEL_URL/split {text} → string[]

  // 2. 각 문장을 임베딩 + Qdrant 저장
  for (const sentence of sentences) {
    const vector = await this.embedService.embed_passage(sentence);  // 1024-dim
    await this.qdrantService.upsertVector('diary_sentence', uuidv4(), vector, {
      diary_id: diary.id,
      memberId: diary.author.id,
      sentence: sentence,   // "[태그] 원문" 형태
      date: diary.written_date
    });
  }
}
```

### 6-2. 검색 (3단계 RAG)

```typescript
// src/diary/diary.service.ts — 검색 라우팅
async getSearchDiary(query: string, memberId: string) {
  if (query.length > SEARCH_KEYWORD_MIN_LENGTH) {
    // 긴 쿼리 → RAG 파이프라인
    return this.sentenceParserService.searchDiaryViaRAG(query, memberId);
  } else {
    // 짧은 쿼리 → 키워드 검색
    return this.keywordService.getDiaryIdBySearchKeyword(query, memberId);
  }
}

// src/sentence-parser/sentence-parser.service.ts — 3단계 RAG
async searchDiaryViaRAG(query: string, memberId: string) {
  // ━━━━ Stage 1: 벡터 검색 (고재현율) ━━━━
  const vector = await this.embedService.embed_query(query);  // "query:" prefix
  const hits = await this.qdrantService.searchVectorByMember(
    'diary_sentence', vector, memberId, 100  // 100개 후보
  );

  // 일기별 중복 제거 (diary_id 기준, 최대 20개)
  const uniqueDiaries = this.deduplicateByDiaryId(hits, 20);

  // ━━━━ Stage 2: 크로스인코더 리랭킹 ━━━━
  const rerankRes = await axios.post(RERANK_MODEL_URL, {
    query,
    candidates: uniqueDiaries.map(d => ({ id: d.id, text: d.sentence }))
  });
  // SEARCH_THRESHOLD (기본 0.4) 이상만 통과
  const filtered = rerankRes.filter(r => r.score >= SEARCH_THRESHOLD);

  // ━━━━ Stage 3: LLM 검증 (날짜 표현 이해) ━━━━
  const ragResult = await this.LLMService.getSearchDiary(query, filtered);
  // Claude가 각 문장의 관련성을 true/false로 판단
  return ragResult.filter(r => r.is_similar).slice(0, SEARCH_TOP_K);  // 최대 10개
}
```

**현재 Spring 상태**: `SentenceVectorPort`과 `SentenceEmbedderPort`가 구현되어 저장은 완료. **검색(RAG) 파이프라인은 아직 미구현**.

**헥사고날 매핑 제안**:
- `DiarySearchUseCase` (In) — 검색 유스케이스
- `RerankPort` (Out) — 리랭킹 (이미 존재)
- `AiSearchValidationPort` (Out) — LLM 검증

---

## 7. 키워드 검색

```typescript
// src/keyword/keyword.service.ts

// 저장: LLM으로 키워드 추출 → SimCSE 임베딩 → Qdrant 배치 저장
async createByDiary(diary: Diary, content: string) {
  const keywords = await this.LLMService.getParsingKeywordDiary(content);
  const unique = [...new Set(keywords)];
  const vectors = await Promise.all(unique.map(kw => this.embedService.embed(kw)));  // 768-dim
  const points = unique.map((kw, i) => ({
    id: uuidv4(),
    vector: vectors[i],
    payload: { memberId: diary.author.id, diaryId: diary.id, keyword: kw }
  }));
  await this.qdrantService.upsertPoints('keyword', points);  // 배치 upsert
}

// 검색: SimCSE 임베딩 → 0.98 이상 유사도 (거의 정확 매칭)
async getDiaryIdBySearchKeyword(keyword: string, memberId: string) {
  const vector = await this.embedService.embed(keyword);
  const results = await this.qdrantService.searchByMemberAndScore(
    'keyword', vector, memberId, 0.98  // 매우 높은 임계값
  );
  return results.map(doc => ({ keyword: doc.payload.keyword, diaryId: doc.payload.diaryId }));
}
```

**현재 Spring 상태**: `KeywordVectorPort`과 `KeywordEmbedderPort` 구현 완료. 저장은 완료되었으나 **검색 로직 미구현**.

---

## 8. 관계 분석 (Relation)

### 관계 그래프 (순수 조회 계층)

```typescript
// src/relation/relation.service.ts

// 관계 그래프 데이터 생성
async getGraph(memberId: string) {
  const graph = await this.getRelation(memberId);
  const todayEmotions = await this.emotionService.getTodayEmotions(memberId);
  return { graph, todayEmotions };
}

private async getRelation(memberId: string) {
  // 1. 전체 인물 조회 (affection 내림차순)
  const targets = await this.targetService.findAll(memberId);
  // 2. 상위 12명만 선택 (count 기준)
  const top12 = targets.sort((a, b) => b.count - a.count).slice(0, 12);

  // 3. 각 인물의 감정 요약 + 시각화 거리 계산
  const relations = [];
  for (const target of top12) {
    const emotionSummary = await this.emotionService.summarizeEmotionsByTarget(target.id);
    const combinedScore = emotionSummary.positive - emotionSummary.negative;
    const distance = normalize(combinedScore, min, max);  // 30~150 범위 (반비례: 가까울수록 작은 값)
    relations.push({ target, distance, emotionSummary });
  }
  return relations;
}
```

**엔드포인트:**
```
GET /relation          → 관계 그래프 데이터
GET /relation/detail/:id → 인물 상세 (감정 요약 + 언급된 일기 목록 + 최근 점수 + 활동 클러스터)
```

**헥사고날 매핑 제안**: `RelationAnalysisUseCase` (In) — 관계 분석 유스케이스. 별도 포트 없이 `PersonQueryPort`와 `EmotionQueryPort`를 조합.

---

## 9. 회원 요약 / 캐릭터 (MemberSummary)

### 감정 요약 업데이트 (일기 생성 시)

```typescript
// src/member/member-summary.service.ts
async updateSummaryFromDiary(peoples, selfEmotions, stateEmotions, member, date) {
  // 모든 감정 소스를 합침 (관계 + 자아 + 상태)
  const allEmotions = [
    ...peoples.flatMap(p => p.interactions).map(e => ({ emotion: e.emotion, intensity: e.intensity })),
    ...selfEmotions,
    ...stateEmotions
  ];

  for (const e of allEmotions) {
    const emotionGroup = getEmotionGroup(e.emotion);  // EmotionType → EmotionGroup 매핑

    // MemberSummary (날짜별) 찾기 or 생성
    let summary = await this.findMemberSummaryIfNotExistCreate(member, date);

    // EmotionSummaryScore (그룹별) 찾기 or 생성
    let score = await this.findEmotionSummaryIfNotExistCreate(summary, emotionGroup);

    // 점수 누적
    score.score += e.intensity;
    score.count += 1;
    await this.emotionSummaryScoreRepo.save(score);
  }

  // 경고 판단 (부정 그룹 점수가 임계값 초과 시)
  await this.checkWarning(member, date);
}
```

### 캐릭터 분류

```typescript
// src/member/member-character.service.ts
async calculateMemberCharacter(memberId: string): string {
  // 1. EmotionBase별 전체 감정 집계
  const baseAnalysis = await this.emotionService.getEmotionBaseAnalysis(memberId);

  // 2. 각 Base에서 최고 감정 추출
  const relationLabel = getRelationLabel(topRelation);   // "연결" or "거리"
  const stateLabel = getStateLabel(topState);             // "고양"/"긴장"/"평온"/"무기력"
  const selfLabel = getSelfLabel(topSelf);                // "긍정" or "부정"

  // 3. (관계, 상태, 자아) 조합 → CHARACTER_MAP에서 동물 캐릭터 조회
  return CHARACTER_MAP[`${relationLabel}-${stateLabel}-${selfLabel}`];
  // 예: "연결-고양-긍정" → "강아지"
}
```

---

## 10. 루틴 추천 (Routine)

### 루틴 추출 (일기 생성 시)

```typescript
// src/analysis/analysis-diary.service.ts
async analysisAndSaveDiaryRoutine(memberId: string, content: string) {
  const member = await this.memberService.findOne(memberId);

  // LLM에게 기분 전환 행동 추출 요청
  const result = await this.promptService.serializeRoutine(content);
  // → { depression: "산책하기" | "None", anger: "음악듣기" | "None", nervous: "None" }

  let created = false;
  for (const [type, routine] of [
    [RoutineEnum.DEPRESSION, result.depression],
    [RoutineEnum.STRESS, result.anger],
    [RoutineEnum.ANXIETY, result.nervous]
  ]) {
    if (routine !== 'None') {
      // 중복 확인 후 저장
      const exists = await this.routineRepo.findOne({ where: { member, content: routine } });
      if (!exists) {
        await this.routineRepo.save({ member, routineType: type, content: routine, isTrigger: true });
        created = true;
      }
    }
  }
  return created;  // true면 알림 발송
}
```

### 루틴 추천 (일기 상세 조회 시)

```typescript
// src/routine/routine.service.ts
async getRecommendRoutine(memberId: string, diaryId: number) {
  // 1. 일기의 대표 감정 그룹 조회
  const emotionGroup = await this.emotionService.getRepresentEmotionGroup(diaryId);

  // 2. EmotionGroup → RoutineEnum 매핑
  //    우울 → DEPRESSION, 불안 → ANXIETY, 스트레스 → STRESS

  // 3. 해당 타입의 루틴 중 랜덤 선택
  const routines = await this.getRoutine(memberId, routineType);
  return routines[Math.floor(Math.random() * routines.length)];
}
```

---

## 11. 컨텐츠 추천 (Recommend)

### 의존 모듈 (가장 많은 의존성)

```
RecommendModule
  ├── EmotionModule    → 기간별/요일별 감정 집계
  ├── DiaryModule      → 일기 조회
  ├── YoutubeModule    → 감정별 영상 조회
  ├── ClaudeModule     → LLM 코멘트 생성
  ├── ActivityClusterModule → 활동 클러스터 조회
  ├── ActivityModule   → 감정 그룹별 활동 조회
  └── NotificationModule [Global] → 추천 알림
```

### 추천 코멘트 생성 (매일 09:00 Cron)

```typescript
// src/recommend/recommend.service.ts
@Cron('0 9 * * *')
async handleCronGetRecommendComment() {
  const members = await this.memberRepo.find();
  for (const member of members) {
    await this.getCommentByWeekday(member.id, LocalDate.now());
  }
}

async getCommentByWeekday(memberId: string, date: LocalDate) {
  // 1. 오늘 요일의 가장 빈번한 부정 감정 그룹
  const emotionGroup = await this.getMostFrequentEmotionGroupByWeekday(memberId, date);

  // 2. 해당 감정 그룹의 긍정 대안 활동 조회
  const activities = await this.activityService.getActivitiesByEmotionGroup(
    memberId, positiveCounterGroup, threshold);

  // 3. 랜덤 활동 선택
  const picked = this.utilService.pickRandomUnique(activities, 1);

  // 4. LLM 코멘트 생성
  const comment = await this.LLMService.getRecommendComment(
    picked.content, emotionGroup, dayOfWeek);

  // 5. 알림 발송
  await this.notificationService.createRecommendNotification(memberId, comment, diaryId);
}
```

### 추천 영상

```typescript
async getRecommendedVideoId(member: Member, periodDays: number) {
  // 기간 내 가장 빈번한 감정 타입 → YouTube 영상 랜덤 3개
  const emotions = await this.emotionService.getAllEmotionsGroupedByDateRange(...);
  const topEmotion = findMostFrequent(emotions);
  return this.youtubeService.getRandomVideoIdByEmotion(topEmotion);
}
```

---

## 12. 할 일 관리 (Todo / DiaryTodo)

### DiaryTodo — AI가 추출한 할 일 (일기 생성 시)

```typescript
// src/diarytodo/diarytodo.service.ts
async createByDiary(todos: string[], diary: Diary, member: Member) {
  const entities = todos.map(content => {
    const todo = new DiaryTodo();
    todo.content = content;
    todo.diary = diary;
    todo.member = member;
    todo.createdAt = diary.written_date;
    return todo;
  });
  await this.diaryTodoRepository.save(entities);
}
```

### TodoCalendar — 사용자가 직접 관리하는 할 일

```typescript
// src/todo/todo.service.ts

// 매일 21:00 미완료 할 일 알림
@Cron('0 21 * * *')
async checkTodoMessage() {
  const todos = await this.todoCalendarRepository.find({
    where: { isCompleted: false, date: LocalDate.now() },
    relations: ['member']
  });
  const memberIds = [...new Set(todos.map(t => t.member.id))];
  for (const memberId of memberIds) {
    await this.notificationService.createTodoNotification(memberId, LocalDate.now());
  }
}
```

**엔드포인트 (14개):**
```
POST   /todos                    → 할 일 생성
GET    /todos                    → 전체 할 일 조회
PATCH  /todos/:id                → 할 일 수정
DELETE /todos/:id                → 할 일 삭제
POST   /todos/calendar           → 캘린더 할 일 생성
GET    /todos/calendar?year=&month= → 월별 캘린더
GET    /todos/calendar/date?date= → 특정 날짜 할 일
PATCH  /todos/calendar/:id       → 완료 토글
PATCH  /todos/calendar/date/:id  → 날짜 변경
PATCH  /todos/calendar/content/:id → 내용 변경
DELETE /todos/calendar/:id       → 캘린더 할 일 삭제
```

---

## 13. 알림 / 웹푸시 (Notification / WebPush)

### NotificationModule — @Global() (모든 모듈에서 주입 가능)

```typescript
// src/notification/notification.service.ts

// 5가지 알림 타입
async createRecommendNotification(memberId, comment, diaryId)  // 추천 코멘트
async createRoutineNotification(memberId)                       // 루틴 발견
async createRecapNotification(memberId, diaryId)                // 작년 오늘 리캡
async createCharacterNotification(memberId)                     // 캐릭터 변경
async createTodoNotification(memberId, targetDate)              // 미완료 할 일

// 공통 생성 로직
async createNotification(memberId, content, type, diaryId?, photoPath?, targetDate?) {
  const member = await this.memberService.findOne(memberId);
  // 1. 웹푸시 발송
  await this.sendWebPush(memberId, content, type, photoPath);
  // 2. DB 저장
  const entity = new NotificationEntity();
  entity.member = member;
  entity.content = content;
  entity.notificationType = type;
  entity.diaryId = diaryId;
  entity.photoPath = photoPath;
  entity.targetDate = targetDate;
  await this.notificationRepo.save(entity);
}
```

### WebpushService — VAPID 기반 웹푸시

```typescript
// src/webpush/webpush.service.ts
async sendNotification(memberId, title, body, iconPath, imagePath?, actions?) {
  const subscriptions = await this.findPushSubscriptions(memberId);
  for (const sub of subscriptions) {
    try {
      await webpush.sendNotification(sub, this.createPayload(title, body, ...));
    } catch (err) {
      if (err.statusCode === 410) {
        sub.isSubscribed = false;  // 자동 구독 해제
        await this.pushRepo.save(sub);
      }
    }
  }
}
```

**헥사고날 매핑 제안**:
- `NotificationCommandPort` (Out) — 알림 저장
- `PushNotificationPort` (Out) — 웹푸시 발송
- `NotificationUseCase` (In) — 알림 조회/읽음 처리

---

## 14. 파일 업로드 (Upload / S3)

```typescript
// src/upload/s3.service.ts
async uploadMultipleFiles(files: Express.Multer.File[]): Promise<string[]> {
  return Promise.all(files.map(file => this.uploadFile(file)));
}

async uploadFile(file: Express.Multer.File): Promise<string> {
  const key = `${uuidv4()}-${file.originalname}`;
  await this.s3Client.send(new PutObjectCommand({
    Bucket: process.env.S3_BUCKET_NAME,
    Key: key,
    Body: file.buffer,
    ContentType: file.mimetype,
  }));
  return `https://${bucket}.s3.amazonaws.com/${key}`;
}

// src/upload/upload.service.ts — 오디오 병합
async uploadAudiosToS3(files) {
  // 1. 임시 파일로 저장
  // 2. FFmpeg로 MP3 병합
  // 3. S3 업로드
  // 4. 임시 파일 정리
}
```

**현재 Spring 상태**: `S3StorageClient` 구현 완료 (`StorageClientPort`). ⚠️ 오디오 병합(FFmpeg)은 미구현.

---

## 15. 성취 클러스터링 (Achievement)

```typescript
// src/achievement-cluster/achievement.service.ts
async createByDiary(achievements: string[], diary: Diary, member: Member) {
  for (const text of achievements) {
    // 1. Qdrant에서 유사 클러스터 검색
    const matches = await this.clusterService.searchTopAchievementCluster(text, member.id);

    if (matches.length > 0 && matches[0].score > THRESHOLD) {
      // 2a. 기존 클러스터에 추가 + 중심벡터 재계산
      await this.clusteringAchievement(matches[0].id, matches[0].payload, diary, text);
    } else {
      // 2b. 새 클러스터 생성 (Qdrant + RDB 양쪽)
      await this.createNewCluster(text, member);
    }
  }
}
```

**패턴**: Vector DB(Qdrant)와 RDB(MySQL)에 이중 저장. 중심벡터(centroid)는 클러스터 내 모든 벡터의 평균으로 재계산.

---

## 16. 활동 클러스터링 (ActivityCluster)

성취 클러스터링과 동일한 패턴. `activity_cluster` Qdrant 컬렉션 (768-dim) 사용.

```typescript
// src/activity-cluster/activity-cluster.service.ts
async createByActivity(activity: Activity, diary: Diary, member: Member) {
  const matches = await this.searchTopActivityCluster(activity.content, member.id);
  if (matches.length > 0) {
    await this.clusteringActivity(matches[0].id, matches[0].payload, diary, activity);
  } else {
    await this.createNewCluster(activity, member);
  }
}
```

---

## 17. 강점 분석 (Strength)

```typescript
// src/strength/strength.service.ts
async getStrengthsSummaryByMember(memberId: string) {
  // Activity 엔티티의 strength 필드를 집계
  // strengthCategoryMap을 통해 카테고리 분류
  const activities = await this.activityRepository.createQueryBuilder('a')
    .where('a.diary.author.id = :memberId', { memberId })
    .andWhere('a.strength IS NOT NULL')
    .getMany();

  const counts = {};
  for (const a of activities) {
    const category = strengthCategoryMap[a.strength] || a.strength;
    counts[category] = (counts[category] || 0) + 1;
  }
  return counts;
}
```

---

## 18. 지도 (Map)

```typescript
// src/map/map.service.ts
async findAllDiaryForMap(memberId: string) {
  return this.diaryRepo.find({
    where: {
      author: { id: memberId },
      latitude: Not(IsNull()),
      longitude: Not(IsNull())
    },
    select: ['id', 'photo_path', 'latitude', 'longitude', 'content']
  });
  // content는 100자까지 잘라서 반환
}
```

---

## 19. YouTube 추천

```typescript
// src/youtube/youtube.service.ts

// Cron으로 YouTube API에서 감정별 영상 수집
async searchAndStoreVideos() {
  for (const [emotion, keywords] of EMOTION_YOUTUBE_KEYWORDS) {
    for (const keyword of keywords) {
      const videos = await this.searchYoutubeVideos(keyword);
      for (const video of videos) {
        await this.saveVideo(emotion, keyword, video.videoId, video.title);
      }
    }
  }
}

// 감정별 랜덤 3개 영상 반환
async getRandomVideoIdByEmotion(emotion: EmotionType): Promise<string[]> {
  const videos = await this.youtubeApiRepository.find({ where: { emotion } });
  return this.utilService.pickRandomUnique(videos, 3).map(v => v.videoId);
}
```

---

## 20. 성능 최적화 마이그레이션 전략

### 20-1. NestJS의 성능 문제점 → Spring에서의 해결 방안

#### 문제 1: 순차 DB 호출 (가장 큰 병목)

**NestJS 원본** (`AnalysisDiaryService`):
```typescript
// 7개 서비스 호출이 모두 순차 실행 (await)
await this.targetService.createByDiary(...);      // 인물 N명 × (findOne + save + emotionTarget)
await this.activityService.createByDiary(...);     // 활동 M개 × (embed + save + cluster)
await this.emotionService.createDiaryStateEmotion(...);
await this.emotionService.createDiarySelfEmotion(...);
await this.memberSummaryService.updateSummaryFromDiary(...);
await this.diaryTodoService.createByDiary(...);
await this.achievementService.createByDiary(...);
```

**Spring 최적화 제안**:
```kotlin
// 독립적인 작업을 coroutineScope로 병렬화
coroutineScope {
    // 그룹 A: Target 먼저 (ActivityTarget이 Target에 의존)
    val targets = async(Dispatchers.IO) { targetService.createByDiary(...) }
    targets.await()

    // 그룹 B: 나머지 독립 작업 병렬
    val jobs = listOf(
        async(Dispatchers.IO) { activityService.createByDiary(...) },
        async(Dispatchers.IO) { emotionService.createDiaryStateEmotion(...) },
        async(Dispatchers.IO) { emotionService.createDiarySelfEmotion(...) },
        async(Dispatchers.IO) { memberSummaryService.updateSummary(...) },
        async(Dispatchers.IO) { diaryTodoService.createByDiary(...) },
        async(Dispatchers.IO) { achievementService.createByDiary(...) },
    )
    jobs.awaitAll()
}
```

#### 문제 2: 문장 임베딩 순차 처리

**NestJS 원본** (`SentenceParserService`):
```typescript
for (const sentence of sentences) {
  const vector = await this.embedService.embed_passage(sentence);  // 순차!
  await this.qdrantService.upsertVector(...);
}
```

**Spring 최적화 제안**:
```kotlin
// 임베딩 병렬화 + 배치 저장
val vectors = coroutineScope {
    sentences.map { sentence ->
        async(Dispatchers.IO) { sentenceEmbedderPort.embedPassage(sentence) }
    }.awaitAll()
}
// Qdrant 배치 upsert (이미 Spring에서 구현됨)
sentenceVectorPort.saveAll(vectors.zip(sentences).map { ... })
```

#### 문제 3: 트랜잭션 부재

**NestJS 원본**: 7개 저장 호출에 트랜잭션 없음. 중간에 실패하면 부분 저장.

**Spring 최적화 제안**:
```kotlin
// RDB 작업은 @Transactional로 묶기
@Transactional
suspend fun persistAnalysisResult(diary: Diary, analysis: DiaryAnalysis) {
    targetService.createByDiary(...)
    emotionService.createDiaryEmotions(...)
    memberSummaryService.update(...)
    diaryTodoService.create(...)
}

// Vector DB 작업은 이벤트로 분리 (이미 DiaryCreateEvent 패턴 적용)
@TransactionalEventListener(phase = AFTER_COMMIT)
fun handleDiaryCreated(event: DiaryCreateEvent) {
    // Qdrant 저장 (실패해도 RDB 롤백 안 됨 → 보상 트랜잭션 고려)
    achievementClusterService.create(...)
    activityClusterService.create(...)
}
```

#### 문제 4: N+1 쿼리

**NestJS 원본**: `for...of` 루프 내에서 `findOne()` + `save()` 반복.

**Spring 최적화 제안**:
```kotlin
// JPA 배치 조회 + saveAll
val existingTargets = targetRepository.findAllByMemberIdAndNameIn(memberId, names)
val targetMap = existingTargets.associateBy { it.name }
// 일괄 업데이트 후
targetRepository.saveAll(updatedTargets)
```

### 20-2. 헥사고날 아키텍처에서의 분석 결과 분해

현재 Spring에서 `metaData`로 JSON 통째 저장 → **이벤트 핸들러에서 개별 엔티티로 분해하는 것을 권장**:

```kotlin
// global/orchestration/DiaryAnalysisEventHandler.kt
@TransactionalEventListener(phase = AFTER_COMMIT)
fun handleDiaryCreated(event: DiaryCreateEvent) {
    val analysis = event.analysisResult

    // 포트를 통한 도메인 분해 (헥사고날 원칙 준수)
    personCommandPort.createFromAnalysis(analysis.peoples, event.diaryId)
    emotionCommandPort.createFromAnalysis(analysis.emotions, event.diaryId)
    activityCommandPort.createFromAnalysis(analysis.activities, event.diaryId)
    todoCommandPort.createFromAnalysis(analysis.todos, event.diaryId)
    achievementCommandPort.createFromAnalysis(analysis.achievements, event.diaryId)
    memberSummaryPort.updateFromAnalysis(analysis.emotions, event.memberId)
}
```

### 20-3. 이벤트 기반 아키텍처 확장 제안

현재 `DiaryCreateEvent` 하나만 있지만, 다음 이벤트를 추가하면 모듈 간 결합도를 낮출 수 있음:

```kotlin
// 이벤트 확장 제안
DiaryCreateEvent         → 분석 결과 분해, RAG 전처리, 키워드 전처리, 루틴 추출
DiaryDeleteEvent         → 벡터 삭제, 관련 엔티티 삭제
EmotionUpdatedEvent      → 캐릭터 재계산, 경고 판단
RoutineCreatedEvent      → 루틴 알림 발송
DailyScheduleEvent       → 추천 코멘트 생성, 미완료 할 일 알림
```

### 20-4. Qdrant 컬렉션 정리

| 컬렉션 | 차원 | 임베딩 모델 | 용도 | Spring Port |
|--------|------|------------|------|-------------|
| `diary_sentence` | 1024 | Dual Encoder (query/passage) | 문장 RAG 검색 | `SentenceVectorPort` ✅ |
| `keyword` | 768 | SimCSE | 키워드 유사 검색 | `KeywordVectorPort` ✅ |
| `activity_cluster` | 768 | SimCSE | 활동 클러스터링 | 미구현 |
| `achievement_cluster` | 768 | SimCSE | 성취 클러스터링 | 미구현 |

### 20-5. 알려진 버그 (마이그레이션 시 수정)

1. **`activity.service.ts`**: self 감정을 `EmotionBase.State`로 저장 (Self여야 함)
2. **`analysis-diary.service.ts`**: `diary.title = 'demo'` 하드코딩 (querySummary 미사용)
3. **`upload.controller.ts`**: 인증 가드 없음 (공개 업로드 가능)
4. **`youtube.controller.ts`**: 인증 가드 없음
5. **`todo.module.ts`**: `MemberService`를 providers에 중복 등록 (MemberModule 임포트와 충돌 가능)
6. **키워드 검색 임계값 0.98**: 하드코딩. 환경변수로 분리 필요
7. **`analysis.module.ts`**: `EmotionModule` 중복 임포트

---

## 부록: NestJS → Spring 모듈 매핑 요약

| NestJS Module | Spring Package | 주요 Port (Out) | 주요 UseCase (In) |
|---------------|---------------|-----------------|-------------------|
| `DiaryModule` | `diary/` | `DiaryRepository`, `DiaryAnalysisPort` | `DiaryCommandUseCase`, `DiaryQueryUseCase` |
| `EmotionModule` | `emotion/` | `EmotionRepository` | `EmotionAnalysisUseCase` |
| `TargetModule` | `person/` | `PersonRepository`, `DiaryPersonLinkPort` | `PersonQueryUseCase` |
| `ActivityModule` | `activity/` | `ActivityRepository`, `ActivityEmbedderPort` | `ActivityQueryUseCase` |
| `RelationModule` | (없음 — 조합) | (EmotionQuery + PersonQuery) | `RelationAnalysisUseCase` |
| `MemberModule` | `member/` | `MemberRepository` | `MemberQueryUseCase` |
| `MemberSummaryService` | `member/` | `MemberSummaryRepository` | `MemberSummaryUseCase` |
| `AnalysisModule` | `diary/` (이벤트) | (분석 결과 분해 포트들) | `DiaryPreprocessingUseCase` |
| `RoutineModule` | `recommend/` | `RoutineRepository` | `RoutineUseCase` |
| `RecommendModule` | `recommend/` | `YoutubeVideoRepository` | `RecommendUseCase` |
| `TodoModule` | `todo/` | `TodoRepository`, `TodoCalendarRepository` | `TodoCommandUseCase` |
| `NotificationModule` | `notification/` | `NotificationRepository`, `PushNotificationPort` | `NotificationUseCase` |
| `UploadModule` | `global/infrastructure/storage/` | `StorageClientPort` ✅ | — |
| `VectorModule` | `global/infrastructure/` | `QdrantClientPort` ✅, `EmbedClientPort` ✅ | — |
| `ClaudeModule` | `global/infrastructure/ai/` | `AiClientPort` ✅ | — |
| `StrengthModule` | `member/` or `activity/` | (ActivityRepository 조합) | `StrengthQueryUseCase` |
| `MapModule` | `diary/` | (DiaryRepository 조합) | `DiaryQueryUseCase` |
