# Harudew API 서버
기존에 nestjs로 만들어져있는 API 서버를 마이그레이션하는 중입니다.
기존 서버 깃허브 링크 -> https://github.com/B1A4-NMM/Remotion-Server

## 기술 스택
- Spring Boot
- Kotlin
- MySQL
- H2
- Spring Security
- Redis
- JPA

## 아키텍처
헥사고날 아키텍처 적용

### 패키지 구조 예시
```markdown
b1a4.harudew.order
├── domain/
│   ├── model/
│   │   └── Order.kt
│   └── service/
│       └── DiscountPolicy.kt
├── application/
│   ├── port/
│   │   ├── in/           (Input Ports / Use Cases)
│   │   │   └── PlaceOrderUseCase.kt
│   │   └── out/          (Output Ports)
│   │       └── OrderRepositoryPort.kt
│   └── service/          (Use Case Implementations)
│       └── PlaceOrderService.kt
└── adapter/
    ├── in/
    │   └── web/          (Web Adapter)
    │       └── OrderController.kt
    └── out/
        └── persistence/  (Persistence Adapter)
            ├── OrderPersistenceAdapter.kt
            └── OrderJpaRepository.kt
```
