# HaruDew API 온보딩 가이드

> **프로젝트 목적**: 기존 NestJS 기반의 [remotion-api](../remotion-api)를 Kotlin + Spring Boot로 마이그레이션\
> **마이그레이션 이유**: 기존 코드의 복잡성과 확장성 한계를 극복하기 위해 헥사고날 아키텍처 적용

---

## 목차

1. [프로젝트 개요](#1-프로젝트-개요)
2. [기술 스택](#2-기술-스택)
3. [아키텍처 이해하기](#3-아키텍처-이해하기)
4. [프로젝트 구조](#4-프로젝트-구조)
5. [개발 환경 셋업](#5-개발-환경-셋업)
6. [빌드 및 실행](#6-빌드-및-실행)
7. [주요 도메인 모델](#7-주요-도메인-모델)
8. [외부 서비스 연동](#8-외부-서비스-연동)
9. [코딩 컨벤션](#9-코딩-컨벤션)
10. [Git 전략](#10-git-전략)
11. [테스트 작성법](#11-테스트-작성법)
12. [마이그레이션 현황](#12-마이그레이션-현황)

---

## 1. 프로젝트 개요

### 서비스 소개
**HaruDew(하루뒤)** 는 AI 기반 일기 분석 및 감정 추적 애플리케이션입니다.

### 핵심 기능
- **일기 작성 및 관리**: 텍스트, 사진, 음성 지원
- **AI 일기 분석**: AWS Bedrock(Claude)을 활용한 자동 감정/활동 추출
- **감정 분석**: 60개 이상의 감정 유형을 3단계 계층으로 분류
- **관계 분석**: 일기에 언급된 사람들과의 관계 추적
- **시맨틱 검색**: Qdrant 벡터 DB를 활용한 의미 기반 검색
- **맞춤 추천**: 감정 패턴 기반 루틴 추천

### 기존 프로젝트(remotion-api)와의 관계
| 구분 | remotion-api | harudew-api |
|------|-------------|-------------|
| 언어 | TypeScript | Kotlin |
| 프레임워크 | NestJS | Spring Boot |
| 아키텍처 | 모듈 기반 | 헥사고날 아키텍처 |
| ORM | TypeORM | Spring Data JPA |
| 상태 | 운영 중 | 개발 중 |

---

## 2. 기술 스택

### 백엔드
- **Spring Boot 3.5.9**: 웹 프레임워크
- **Kotlin 2.2.21**: 프로그래밍 언어
- **Spring Data JPA**: 데이터 접근 계층
- **Spring Security**: 인증/인가
- **Spring AI**: AWS Bedrock 연동

### 데이터베이스
- **H2** (개발): 인메모리 DB
- **MySQL** (운영): 관계형 DB
- **Qdrant**: 벡터 데이터베이스 (시맨틱 검색)

### AI/ML
- **AWS Bedrock (Claude Sonnet 4)**: 일기 분석 LLM
- **외부 임베딩 서비스**: 텍스트 벡터화

### 테스트
- **JUnit 5**: 테스트 프레임워크
- **MockitoKotlin**: 모킹 라이브러리
- **AssertJ**: 어설션 라이브러리

---

## 3. 아키텍처 이해하기

### 헥사고날 아키텍처란?

**헥사고날 아키텍처**(Hexagonal Architecture), 또는 **포트와 어댑터 패턴**(Ports and Adapters)은 비즈니스 로직을 외부 의존성으로부터 분리하는 소프트웨어 아키텍처입니다.

```
                    ┌─────────────────────────────────────┐
                    │           인바운드 어댑터            │
                    │  (Web Controller, CLI, Message...)  │
                    └──────────────┬──────────────────────┘
                                   │
                                   ▼
                    ┌──────────────────────────────────────┐
                    │         인바운드 포트 (Use Case)      │
                    │    interface CreateDiaryUseCase      │
                    └──────────────┬───────────────────────┘
                                   │
                    ┌──────────────▼───────────────────────┐
                    │                                      │
                    │          도메인 (핵심 비즈니스)        │
                    │     - 엔티티 (Diary, Member...)      │
                    │     - 비즈니스 규칙                   │
                    │                                      │
                    └──────────────┬───────────────────────┘
                                   │
                    ┌──────────────▼───────────────────────┐
                    │        아웃바운드 포트 (추상화)        │
                    │    interface DiaryRepository         │
                    │    interface DiaryAnalysisPort       │
                    └──────────────┬───────────────────────┘
                                   │
                                   ▼
                    ┌─────────────────────────────────────┐
                    │          아웃바운드 어댑터           │
                    │  (JPA, HTTP Client, Vector DB...)  │
                    └─────────────────────────────────────┘
```

### 왜 헥사고날 아키텍처를 사용하나요?

1. **테스트 용이성**: 비즈니스 로직을 외부 의존성 없이 테스트 가능 (매우 중요)
2. **유연한 교체**: 데이터베이스나 외부 서비스를 쉽게 교체 가능 (매우 중요)
3. **관심사 분리**: 각 계층이 명확한 책임을 가짐
4. **비즈니스 로직 보호**: 도메인 코드가 프레임워크에 의존하지 않음

### 계층별 역할

| 계층 | 역할 | 예시 |
|------|------|------|
| **Domain** | 순수한 비즈니스 로직, 엔티티 | `Diary`, `Member`, `Emotion` |
| **Application** | 유스케이스 조율, 포트 정의 | `CreateDiaryUseCase`, `DiaryAnalysisPort` |
| **Adapter** | 외부 시스템과의 연결 | `DiaryController`, `DiaryJpaAdapter` |

---

## 4. 프로젝트 구조

### 전체 구조
```
src/main/kotlin/b1a4/harudew/
├── diary/                 # 일기 도메인 (핵심)
├── emotion/               # 감정 도메인
├── member/                # 회원 도메인
├── activity/              # 활동 도메인
├── todo/                  # 할일 도메인
├── person/                # 인물 도메인
├── achievement/           # 성취 도메인
├── notification/          # 알림 도메인
├── recommend/             # 추천 도메인
├── target/                # 목표 도메인
└── global/                # 공통 인프라
```

### 도메인 모듈 구조 (diary 예시)
```
diary/
├── domain/
│   ├── model/
│   │   └── Diary.kt           # 도메인 엔티티 (순수 Kotlin 클래스)
│   └── service/
│       └── DiaryDomainService.kt  # 도메인 서비스
│
├── application/
│   ├── port/
│   │   ├── in/               # 인바운드 포트 (유스케이스)
│   │   │   ├── DiaryCommandUseCase.kt
│   │   │   └── DiaryQueryUseCase.kt
│   │   └── out/              # 아웃바운드 포트 (추상화)
│   │       ├── persistence/
│   │       │   └── DiaryRepository.kt
│   │       ├── analysis/
│   │       │   └── DiaryAnalysisPort.kt
│   │       └── vector/
│   │           └── KeywordVectorPort.kt
│   └── service/
│       ├── DiaryCommandService.kt  # 유스케이스 구현
│       └── DiaryQueryService.kt
│
└── adapter/
    ├── in/
    │   └── web/
    │       ├── DiaryController.kt      # REST API
    │       └── dto/
    │           ├── CreateDiaryRequest.kt
    │           └── DiaryResponse.kt
    └── out/
        ├── persistence/
        │   ├── DiaryJpaAdapter.kt      # JPA 구현
        │   ├── DiaryJpaRepository.kt
        │   └── entity/
        │       └── DiaryEntity.kt      # JPA 엔티티
        ├── ai/
        │   └── DiaryAnalysisAdapter.kt # AI 분석 구현
        └── qdrant/
            └── QdrantKeywordVectorAdapter.kt
```

### 데이터 흐름 예시: 일기 생성

```
1. HTTP 요청 → DiaryController (인바운드 어댑터)
2. DTO → DiaryCommandUseCase.create() (인바운드 포트)
3. DiaryCommandService가 비즈니스 로직 실행
4. DiaryAnalysisPort.analyze() 호출 (아웃바운드 포트)
5. DiaryAnalysisAdapter가 AWS Bedrock 호출 (아웃바운드 어댑터)
6. DiaryRepository.save() 호출 (아웃바운드 포트)
7. DiaryJpaAdapter가 DB 저장 (아웃바운드 어댑터)
```

---

## 5. 개발 환경 셋업

### 필수 프로그램

#### 1. JDK 17

**Windows (Chocolatey)**
```powershell
choco install temurin17
```

**macOS (Homebrew)**
```bash
# Homebrew가 없다면 먼저 설치
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# JDK 17 설치
brew install --cask temurin@17
```

또는 https://adoptium.net/ 에서 직접 다운로드

설치 확인:
```bash
java -version
# openjdk version "17.x.x" 이상
```

#### 2. IntelliJ IDEA
- **Ultimate** 권장 (Spring 지원), Community도 가능
- 다운로드: https://www.jetbrains.com/idea/

#### 3. 필수 IntelliJ 플러그인
- **Kotlin** (기본 포함)
- **Spring Boot** (Ultimate 기본 포함)
- **Database Tools** (Ultimate 기본 포함)

### 프로젝트 열기

1. IntelliJ IDEA 실행
2. `File > Open` → 프로젝트 폴더 선택
3. Gradle 프로젝트로 인식되면 자동으로 의존성 다운로드
4. `File > Project Structure > Project` 에서 SDK를 JDK 17로 설정

### 환경 설정 파일

#### application-local.yml 설정
`src/main/resources/application-local.yml` 파일을 확인하고 필요시 수정:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  ai:
    bedrock:
      aws:
        region: ap-northeast-2
        access-key: ${AWS_ACCESS_KEY}     # 환경변수로 설정 또는 직접 입력
        secret-key: ${AWS_SECRET_KEY}

# Qdrant 설정
qdrant:
  host: goochul.iptime.org
  port: 6334

# 외부 ML 서비스
parser:
  model:
    url: http://goochul.iptime.org:5006/split

embed:
  simple:
    url: http://goochul.iptime.org:5004/embed
  cross:
    url: http://goochul.iptime.org:5002/rerank
  dual:
    url: http://goochul.iptime.org:5001/embed
```

*주의사항*\
절대절대절대절대 위 설정파일을 git에 추가하지 말 것 !!!\
main/resource에는 application-local.yml\
test/resource에는 application.yml 추가하기

### 환경 변수 설정

**Windows (PowerShell)**
```powershell
# 세션 동안만 유효
$env:AWS_ACCESS_KEY="your-access-key"
$env:AWS_SECRET_KEY="your-secret-key"

# 영구 설정
[System.Environment]::SetEnvironmentVariable("AWS_ACCESS_KEY", "your-access-key", "User")
[System.Environment]::SetEnvironmentVariable("AWS_SECRET_KEY", "your-secret-key", "User")
```

**macOS / Linux (zsh/bash)**
```bash
# 세션 동안만 유효
export AWS_ACCESS_KEY="your-access-key"
export AWS_SECRET_KEY="your-secret-key"

# 영구 설정 (~/.zshrc 또는 ~/.bashrc에 추가)
echo 'export AWS_ACCESS_KEY="your-access-key"' >> ~/.zshrc
echo 'export AWS_SECRET_KEY="your-secret-key"' >> ~/.zshrc
source ~/.zshrc
```

**또는 IntelliJ Run Configuration에서 설정 (모든 OS 공통)**
1. `Run > Edit Configurations`
2. Spring Boot 설정 선택
3. `Environment variables` 필드에 `AWS_ACCESS_KEY=xxx;AWS_SECRET_KEY=xxx` 추가

---

## 6. 빌드 및 실행

### Gradle 명령어

> **macOS/Linux 첫 실행 시**: gradlew 실행 권한이 없으면 아래 명령어로 권한 부여
> ```bash
> chmod +x ./gradlew
> ```

```bash
# 전체 빌드
./gradlew build

# 테스트 제외하고 빌드
./gradlew build -x test

# 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "DiaryAnalysisAdapterTest"

# 애플리케이션 실행
./gradlew bootRun

# local 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

### IntelliJ에서 실행

1. `HarudewApiApplication.kt` 파일 열기
2. `main` 함수 왼쪽의 ▶ 버튼 클릭
3. 또는 `Shift + F10`

### 프로파일 설정

| 프로파일 | 용도 | 데이터베이스 |
|---------|------|-------------|
| `local` | 로컬 테스트 | H2 + 외부 서비스 연동 |

---

## 7. 주요 도메인 모델

### Member (회원)
```kotlin
class Member(
    val id: String,
    val email: String,
    val nickname: String,
    val socialType: SocialType,  // GOOGLE, KAKAO
    val character: String,
    val lastStressTestDate: LocalDate?,
    val lastAnxietyTestDate: LocalDate?,
    val lastDepressionTestDate: LocalDate?
)
```

### Diary (일기)
```kotlin
class Diary(
    val id: Long,
    val author: Member,
    val createAt: LocalDateTime,
    val writtenDate: LocalDate,
    val content: String,
    val title: String,
    val weather: String,
    val photoPath: List<String>,
    val audioPath: List<String>,
    val isBookmark: Boolean,
    val latitude: Double,
    val longitude: Double,
    val metaData: String  // AI 분석 결과 (JSON)
)
```

### Emotion (감정) - 3단계 계층 구조

```
EmotionBase (최상위)
├── RELATION (관계 기반 감정) - 22개
│   ├── EmotionGroup: 유대, 스트레스
│   └── 감사, 신뢰, 애정, 분노, 질투, 배신감...
│
├── SELF (자아 기반 감정) - 10개
│   ├── EmotionGroup: 활력, 스트레스
│   └── 자신감, 성취감, 후회, 죄책감...
│
└── STATE (상태 기반 감정) - 28개
    ├── EmotionGroup: 활력, 안정, 불안, 우울
    └── 행복, 평온, 불안, 우울, 피로...
```

**EmotionGroup** (6가지):
- 활력 (Vitality)
- 안정 (Stability)
- 유대 (Bond)
- 스트레스 (Stress)
- 불안 (Anxiety)
- 우울 (Depression)

---

## 8. 외부 서비스 연동

### AWS Bedrock (Claude)
- **용도**: 일기 내용 AI 분석
- **모델**: Claude Sonnet 4
- **분석 내용**: 감정 추출, 활동 분류, 키워드 추출, 태깅

### Qdrant (벡터 DB)
- **용도**: 시맨틱 검색
- **저장 데이터**: 키워드 벡터, 문장 벡터
- **검색**: 의미 기반 유사 일기 검색

### 외부 ML 서비스
| 서비스 | 포트 | 용도 |
|--------|------|------|
| Embedding | 5004 | 텍스트 → 벡터 변환 |
| Dual Embed | 5001 | 쿼리/문서 별도 임베딩 |
| Rerank | 5002 | 검색 결과 재순위화 |
| Parser | 5006 | 문장 분리 |

---

## 9. 코딩 컨벤션

### Kotlin 스타일
- [Kotlin 공식 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html) 준수
- IntelliJ 기본 포맷터 사용 (`Ctrl + Alt + L`)

### 네이밍 규칙
```kotlin
// 클래스: PascalCase
class DiaryCommandService

// 함수/변수: camelCase
fun createDiary(command: CreateDiaryCommand)

// 상수: SCREAMING_SNAKE_CASE
const val MAX_PHOTO_COUNT = 10

// 패키지: lowercase
package b1a4.harudew.diary.application.service
```

### JPA 엔티티 패턴
```kotlin
@Entity
@Table(name = "diary")
class DiaryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val content: String,

    // ... 다른 필드
) {
    // 도메인 변환
    fun toDomain(): Diary = Diary(
        id = id,
        content = content,
        // ...
    )

    companion object {
        fun fromDomain(diary: Diary): DiaryEntity = DiaryEntity(
            id = diary.id,
            content = diary.content,
            // ...
        )
    }
}
```

### 포트 인터페이스 네이밍
```kotlin
// 인바운드 포트 (유스케이스)
interface DiaryCommandUseCase  // 명령 (생성, 수정, 삭제)
interface DiaryQueryUseCase    // 조회

// 아웃바운드 포트
interface DiaryRepository      // 저장소
interface DiaryAnalysisPort    // 외부 서비스 (Port 접미사)
```

---

## 10. Git 전략

### 브랜치 구조
```
main                    # 운영 배포용
├── develop             # 개발 통합
├── feature/xxx         # 기능 개발
├── fix/xxx             # 버그 수정
└── refactor/xxx        # 리팩토링
```

### 브랜치 네이밍
```bash
feature/diary-search        # 기능 개발
fix/emotion-parsing-error   # 버그 수정
refactor/member-entity      # 리팩토링
```

### 커밋 메시지 컨벤션 (권장)

> 반드시 따라야 하는 규칙은 아니지만, 일관성을 위해 아래 형식을 권장합니다.

```
<type>(<scope>): <subject>
```

**실제 사용 예시** (기존 커밋 내역 참고):
```bash
feat(Qdrant): Qdrant config 추가
feat(Rerank): 리랭크 어댑터 작성
test(Embed): 임베딩 어댑터 단위 테스트 작성
refactor(diary): 리패키징
refactor(*): 패키지 변경에 따른 의존 패키지 경로 변경
build(build.gradle): awaitility 비동기 테스트 라이브러리 추가
docs(readme): readme 수정
chore(gitignore): 깃이그노어 추가
in-progress(Qdrant): Qdrant 관련 포트 작성
```

**타입**:\
| 타입 | 설명 |\
|------|------|\
| `feat` | 새로운 기능 추가 |\
| `fix` | 버그 수정 |\
| `refactor` | 리팩토링 (기능 변경 없음) |\
| `test` | 테스트 추가/수정 |\
| `build` | 빌드 설정, 의존성 변경 |\
| `docs` | 문서 수정 |\
| `chore` | 기타 잡무 (gitignore 등) |\
| `in-progress` | 작업 중인 기능 (임시 커밋) |

**스코프 작성 팁**:
- 모듈/기능명 사용: `Qdrant`, `Embed`, `diary`, `Rerank`
- 파일명 사용 가능: `build.gradle`, `readme`
- 여러 모듈 변경 시: `*` 사용

### 작업 흐름
```bash
# 1. develop에서 feature 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b feature/my-feature

# 2. 작업 후 커밋
git add .
git commit -m "feat(diary): 일기 검색 기능 구현"

# 3. 원격 저장소에 푸시
git push origin feature/my-feature

# 4. PR 생성 및 코드 리뷰

# 5. develop으로 머지
```

### 코드 리뷰 체크리스트
- [ ] 헥사고날 아키텍처 원칙 준수
- [ ] 테스트 코드 작성
- [ ] 코딩 컨벤션 준수
- [ ] 불필요한 의존성 추가 없음
- [ ] 도메인 로직이 어댑터에 누출되지 않음

---

## 11. 테스트 작성법

### 테스트 구조
```
src/test/kotlin/b1a4/harudew/
├── diary/
│   └── adapter/
│       └── out/
│           ├── ai/
│           │   └── DiaryAnalysisAdapterTest.kt
│           └── persistence/
│               └── DiaryJpaAdapterTest.kt
└── ...
```

### 단위 테스트 예시
```kotlin
@ExtendWith(MockitoExtension::class)
class DiaryCommandServiceTest {

    @Mock
    lateinit var diaryRepository: DiaryRepository

    @Mock
    lateinit var diaryAnalysisPort: DiaryAnalysisPort

    @InjectMocks
    lateinit var diaryCommandService: DiaryCommandService

    @Test
    fun `일기 생성 시 AI 분석이 호출되어야 한다`() {
        // given
        val command = CreateDiaryCommand(
            content = "오늘 좋은 하루였다.",
            title = "행복한 하루"
        )
        val analysisResult = DiaryAnalysis(...)

        given(diaryAnalysisPort.analyze(any()))
            .willReturn(analysisResult)
        given(diaryRepository.save(any()))
            .willReturn(1L)

        // when
        val diaryId = diaryCommandService.create(command)

        // then
        assertThat(diaryId).isEqualTo(1L)
        verify(diaryAnalysisPort).analyze(any())
        verify(diaryRepository).save(any())
    }
}
```

### given/when/then 패턴
- **given**: 테스트에 필요한 사전 조건 설정
- **when**: 테스트 대상 동작 실행
- **then**: 결과 검증 (assertThat 사용)

### 어설션 예시 (AssertJ)
```kotlin
// 단순 비교
assertThat(result).isEqualTo(expected)

// 컬렉션
assertThat(list).hasSize(3)
assertThat(list).contains(item)
assertThat(list).containsExactly(item1, item2)

// 예외
assertThatThrownBy { service.doSomething() }
    .isInstanceOf(IllegalArgumentException::class.java)
    .hasMessage("잘못된 입력입니다")
```

---

## 12. 마이그레이션 현황

### 완료된 모듈

| 모듈 | 상태 | 비고 |
|------|------|------|
| 프로젝트 구조 | ✅ 완료 | 헥사고날 아키텍처 적용 |
| global (인프라) | 🔄 진행 중 | AI, 임베딩, 설정 |
| emotion | 🔄 진행 중 | 60+ 감정 타입 정의 |
| member | 🔄 진행 중 | 기본 모델 완료 |
| diary | 🔄 진행 중 | AI 분석 연동 완료 |

### 진행 중인 작업

- [ ] Qdrant 벡터 저장/검색 기능
- [ ] 일기 CRUD API
- [ ] 회원 인증 (OAuth)

### 남은 모듈

| 모듈 | 우선순위 | 예상 복잡도 |
|------|----------|------------|
| activity | 높음 | 중간 |
| todo | 중간 | 낮음 |
| person | 중간 | 낮음 |
| notification | 낮음 | 중간 |
| recommend | 낮음 | 높음 |

---

## 참고 자료

### Kotlin
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [Kotlin Koans (실습)](https://play.kotlinlang.org/koans)

### Spring Boot
- [Spring Boot 공식 문서](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA 가이드](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

### 헥사고날 아키텍처
- [Hexagonal Architecture 설명 (영문)](https://alistair.cockburn.us/hexagonal-architecture/)
- [Get Your Hands Dirty on Clean Architecture (책)](https://www.amazon.com/dp/1839211962)

---

> **질문이 있으면 언제든 물어보세요!**
