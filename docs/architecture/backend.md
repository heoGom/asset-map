# Asset Map Backend Architecture

Spring Boot 기반의 Asset Map 백엔드는 견고한 자산 관리 도메인 모델과 고도화된 통계 분석 API를 제공합니다.

## 기본 정보
- **경로**: `asset-map/backend`
- **엔트리포인트**: `com.assetmap.backend.BackendApplication`
- **공통 규칙**: `docs/common/rules/` (도메인 설계, API 가이드라인 참조)

## 기술 스택 (Tech Stack)

| Category | Technology | Description |
| --- | --- | --- |
| **Framework** | Spring Boot 3.5.6 | 최신 안정 버전 기반 |
| **Language** | Java 17 | 레코드 및 최신 문법 활용 |
| **Build Tool** | Gradle | 의존성 및 빌드 관리 |
| **Database** | H2 | Local/Test용 인메모리 DB |
| **ORM** | Spring Data JPA | 도메인 모델 영속화 |
| **Utilities** | Lombok, Validation | 코드 생산성 및 데이터 검증 |

## 도메인 모델 (Domain Model)

| 엔티티 | 설명 | 주요 필드 |
| --- | --- | --- |
| `Account` | 증권 계좌 정보 | `name`, `accountNumber`, `accountType`, `brokerName` |
| `SecurityItem` | 종목 마스터 정보 | `ticker`, `name`, `country`, `sector`, `securityType` |
| `Holding` | 실제 자산 보유 현황 | `account`, `securityItem`, `quantity`, `averagePrice` |
| `SecurityClassification` | 자산 분류 체계 | `assetGroup`, `sector`, `countryGroup`, `strategyType` |
| `HoldingSnapshot` | 시점별 자산 기록 | `snapshotDate`, `totalValue`, `holdingsJson` |
| `DividendEvent` | 배당 예정 정보 | `securityItem`, `exDividendDate`, `dividendPerShare` |
| `DividendPayment` | 실제 배당 지급 내역 | `holding`, `paymentDate`, `amount`, `status` |

### 주요 Enum 정의

- **AccountType**: `CASH`, `STOCK`, `ISA`, `PENSION`, `IRP`
- **SecurityType**: `COMMON_STOCK`, `PREFERRED_STOCK`, `ETF`, `REIT`, `BOND`
- **DividendPaymentStatus**: `SCHEDULED`, `PAID`, `CANCELLED`
- **DataSourceType**: `MANUAL`, `API_CRAWLING`

## 패키지 구조 (Package Structure)

```text
com.assetmap.backend
├── account         # 계좌 CRUD 및 관리
├── securityitem    # 종목 마스터 정보 관리
├── holding         # 보유 수량 및 평단가 관리
├── classification  # 종목별 다각도 분류 (섹터, 전략 등)
├── dividend        # 배당 스케줄링 및 지급 이력
├── snapshot        # 자산 추이 추적을 위한 스냅샷
├── dashboard       # 통합 자산/배당 통계 API
├── common          # ApiResponse, GlobalExceptionHandler, BaseEntity
└── config          # JpaAuditing, 보안 설정 등
```

## 주요 API 명세 (Key API Endpoints)

### 1. Dashboard & Analysis
- `GET /api/dashboard/summary`: 전체 자산 합계, 평가 손익 요약
- `GET /api/dashboard/ratios`: 자산군별/국가별 비중 통계
- `GET /api/dividend/summary`: 연간 예상 배당금 및 배당률 요약
- `GET /api/dividend/monthly`: 월별 배당 달력 데이터

### 2. Asset Management
- `GET /api/accounts`: 계좌 목록 조회
- `POST /api/holdings`: 새로운 보유 종목 등록
- `GET /api/securities/search?keyword=...`: 종목 검색

### 3. History
- `GET /api/snapshots/timeline`: 기간별 자산 가치 변동 추이
- `POST /api/snapshots/capture`: 현재 시점의 자산 스냅샷 수동 저장

## 구현 상세

### 데이터 감사 (Audit)
- 모든 엔티티는 `BaseEntity`를 상속받아 `createdAt`, `updatedAt`을 자동으로 관리합니다.
- `JpaAuditingConfig`를 통해 활성화되어 있습니다.

### 예외 처리 및 응답 규격
- `ApiResponse<T>` 클래스를 통해 모든 API 응답을 표준화 (`status`, `message`, `data`).
- `GlobalExceptionHandler`에서 `BusinessException` 및 시스템 예외를 통합 관리합니다.

### 데이터 계산 로직
- `MoneyCalculator` 유틸리티를 통해 자산 가치 합산 및 수익률 계산 로직을 분리하여 관리합니다.
- 부동 소수점 오차 방지를 위해 금액 계산 시 `BigDecimal` 사용을 원칙으로 합니다.

## 실행 및 검증 (Run & Verify)

### 로컬 실행
```bash
cd backend
./gradlew bootRun
```
- API Base: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console/` (JDBC URL: `jdbc:h2:mem:assetdb`)

### 테스트 수행
```bash
./gradlew test
```
- JUnit 5 기반의 단위 테스트 및 `MockMvc`를 활용한 API 테스트가 포함되어 있습니다.
