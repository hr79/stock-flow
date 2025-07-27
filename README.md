# 📦 Stock Flow

## 📌 프로젝트 개요

**StockFlow**는 중소 제조·유통 기업을 위한 **B2B 재고 흐름 관리 백엔드 시스템**입니다. 발주 → 입고 → 재고 → 출고의 실무 흐름을 반영한 도메인 기반 아키텍처로 설계했으며,
단순 CRUD를 넘어 대량 출고 처리 시 멀티스레딩 기반 성능 최적화와 재고 임계치 알림(Discord Webhook) 기능을 구현했습니다.


> 본 프로젝트는 백엔드 단독 구성으로, UI 없이 API 단위의 데이터 흐름 구현에 집중되어 있습니다.

  <br>

### 🏢 **B2B 시나리오 (기업 내부 프로세스 자동화)**

| 단계         | 설명                                                  |
| ---------- |-----------------------------------------------------|
| 1️⃣ 발주 등록  | 구매 담당자가 공급업체에 A상품 100개 주문                           |
| 2️⃣ 입고 처리  | 공급업체가 상품을 보내오면, 창고에 입고됨                             |
| 3️⃣ 재고 반영  | 재고 수량 +100                                          |
| 4️⃣ 출고 처리  | 다른 지점에 보내거나 고객 주문 시 출고 → 재고 감소                      |
| 5️⃣ 임계치 알림 | 출고 후 재고가 설정된 임계치 이하로 떨어지면 Discord Webhook 실시간 알림 발송 |

<br>

## 🔗 ERD
![erd](https://github.com/user-attachments/assets/c24a427e-bfd3-4751-a17d-2e9d440730ac)

- PurchaseOrder (1) - (N) PurchaseOrderItem

- OutboundOrder (1) - (N) OutboundRequestItem

- Product (1) - (N) Stock(Item)
<br>

## 🛠 기술 스택

- Java 21
- Spring Boot 3.2
- Spring Data JPA (Hibernate)
- H2 Database
- Springdoc OpenAPI (Swagger UI)

<br>

## 💡 주요 기능 및 흐름

| 기능                    | 설명                                                                                                    |
|-----------------------|-------------------------------------------------------------------------------------------------------|
| 발주 생성 (PurchaseOrder) | 품목 리스트를 기반으로 총액 계산 후 저장                                                                               |
| 입고 처리 (Inbound)       | 발주 기준으로 입고 → 재고 수량 증가                                                                                 |
| 출고 요청 (OutboundOrder) | 현재 재고량을 비교하여 출고 가능 여부 판단                                                                              |
| 출고 완료 처리 (Outbound)   | 출고 이력 저장 및 재고 수량 차감                                                                                   |
| 예외 처리                 | 잘못된 요청/재고 부족 시 일관된 에러 메시지 반환                                                                          |
| API 문서화               | Swagger UI 자동 생성, `/swagger-ui.html` 제공 <br> [자세히](https://hr79.github.io/stock-flow-api-swagger-ui/) |

<br>

## 📈 Enhancement

### ✅ 멀티스레딩 기반 출고 처리
- 적용 이유: 단일 스레드 방식으로는 10,000건 이상의 출고 요청 처리 시 성능 저하 발생

- 구현 방식:
  - CompletableFuture + 전용 ExecutorService

  - 트랜잭션 격리 → @Transactional(propagation = REQUIRES_NEW) 적용
  - EntityManager 스레드 안전성 해결 → 스레드별 DB 조회 전략

- 결과 (테스트 환경: 10,000건 요청 / 10 Core, 32GB 메모리)

  - Before: 2.77초 → After: 0.65초 (약 76% 향상)

- 해결한 문제:
  - LazyInitializationException → fetch join + DTO 변환

  - 트랜잭션 충돌 → 클래스 분리 및 REQUIRES_NEW 적용
- 작업 흐름:
  ```aiignore
  출고 처리(List<ProductDto>)
  │
  ├─> requestItem 조회 및 데이터 매핑
  │
  ├─> CompletableFuture.supplyAsync() → 스레드별 출고 처리
  │         │
  │         ├─> 신규 트랜잭션 시작(REQUIRES_NEW)
  │         └─> 재고 차감 + 기록 저장
  │
  └─> join() 결과 취합 → 응답
  ```

- 자세히: [멀티스레딩 기반 출고 처리 성능 최적화 (Spring Boot + JPA) (Notion)](https://www.notion.so/Spring-Boot-JPA-23be74104e0880c38dbfcc284be84efe?source=copy_link)

### ✅ 재고 임계치 알림 (Discord Webhook)
- 적용 이유: 실시간 재고 모니터링 → 운영자가 즉시 대응 가능해야 함

- 구현 방식:
  - 출고 처리 완료 후, 재고 수량 체크
  - 임계치 이하 → Discord Webhook API 호출 (content 메시지 전달)
  - WebClient + 비동기 처리(subscribe())로 알림 발송 → 트랜잭션 지연 방지

- 특징:
  - 후처리 로직 → 트랜잭션 안정성 보장 
  - 확장성 → Slack, Email 알림으로 쉽게 전환 가능

- 예시 알림:
    ![discord-notification](https://github.com/user-attachments/assets/ff76583e-72ec-4c59-ab72-a718922e50d8)
<br>

## 🐞Bug Fixing
| 문제                              | 원인                        | 해결책                                    |
| ------------------------------- | ------------------------- | -------------------------------------- |
| **LazyInitializationException** | JPA 프록시 지연 로딩             | fetch join 적용                  |
| **멀티스레딩 트랜잭션 충돌**               | 동일 트랜잭션 공유로 DB Lock 발생    | `@Transactional(REQUIRES_NEW)` + 클래스 분리 |
| **EntityManager 스레드 안전성**       | 멀티스레드 환경에서 영속성 컨텍스트 접근 가능성 | 작업 시작 전 모든 데이터 사전 로딩 후, Map 기반 병렬 처리                       |


## 🧩 패키지 구조
```
.
├── common
├── domain
│   ├── inbound
│   ├── outbound
│   │   ├── dto
│   │   └── service
│   ├── product
│   ├── purchaseorder
│   │   └── dto
│   └── supplier
├── model
└── notification
```

<br>

## 🌱 향후 확장 포인트

 - 사용자 인증/인가 추가 (JWT 기반 로그인)
 - 출고 요청 승인 프로세스 (관리자 역할 추가)
 - 이메일, Slack 등 자동화/알림 시스템 추가(Webhook 또는 메일)

<br>

## 📜 API 명세서

[API 명세서 보러가기(Swagger)](https://hr79.github.io/stock-flow-api-swagger-ui/)