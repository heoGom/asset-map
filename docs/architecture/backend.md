# Asset Map Backend Architecture

Spring Boot 기반의 Asset Map 백엔드는 거래내역을 원천 데이터로 사용하고, 현재 보유 현황과 배당 지급 예정 데이터를 계산하는 구조를 제공합니다.

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
| `Account` | 증권/연금/현금 계좌 정보 | `userId`, `name`, `brokerName`, `accountType`, `currency` |
| `SecurityItem` | 종목 마스터 정보 | `ticker`, `name`, `market`, `country`, `securityType` |
| `TradeTransaction` | 매수/매도/초기보유 원천 거래내역 | `tradeDate`, `tradeType`, `quantity`, `price`, `netAmount` |
| `Holding` | 거래내역으로 갱신되는 현재 보유 상태 캐시 | `account`, `securityItem`, `quantity`, `averagePrice`, `currentPrice` |
| `SecurityClassification` | 자산 분류 체계 | `assetGroup`, `sector`, `countryGroup`, `listingCountry`, `exposureCountry`, `underlyingIndex`, `hedged` |
| `HoldingSnapshot` | 시점별 자산 기록 | `snapshotDate`, `evaluatedAmountKrw` |
| `DividendEvent` | 종목별 배당 이벤트 | `declarationDate`, `exDividendDate`, `recordDate`, `paymentDate`, `dividendPerShare` |
| `DividendPayment` | 사용자별 예상/확정/지급 배당 내역 | `quantityAtRecordDate`, `grossAmount`, `netAmount`, `status` |
| `MarketPrice` | 향후 시세 연동을 위한 가격 이력 | `priceDate`, `currentPrice`, `source`, `fetchedAt` |

### 주요 Enum 정의

- **AccountType**: `GENERAL`, `ISA`, `PENSION`, `IRP`, `CMA`, `SAVINGS`, `CRYPTO`, `CASH`
- **SecurityType**: `STOCK`, `ETF`, `REIT`, `BOND`, `CASH`, `CRYPTO`
- **TradeType**: `INITIAL`, `BUY`, `SELL`
- **DividendPaymentStatus**: `EXPECTED`, `CONFIRMED`, `PAID`
- **DataSourceType**: `MANUAL`, `API`, `CSV`, `CRAWLING`
- **MarketDataSource**: `MANUAL`, `PUBLIC_DATA`, `KRX`, `BROKER_API`

## 패키지 구조 (Package Structure)

```text
com.assetmap.backend
├── account         # 계좌 CRUD 및 관리
├── securityitem    # 종목 마스터 정보 관리
├── transaction     # 거래 원천 데이터 및 Holding 자동 갱신
├── holding         # 보유 수량 및 평단가 관리
├── classification  # 종목별 다각도 분류 (섹터, 전략 등)
├── dividend        # 배당 스케줄링 및 지급 이력
├── marketprice     # 가격 이력 및 외부 시세 provider 구조
├── snapshot        # 자산 추이 추적을 위한 스냅샷
├── dashboard       # 통합 자산/배당 통계 API
├── common          # ApiResponse, GlobalExceptionHandler, BaseEntity
└── config          # JpaAuditing, 보안 설정 등
```

## 주요 API 명세 (Key API Endpoints)

### 1. Portfolio Source Data
- `POST /api/trades`: 거래 등록 및 Holding 자동 갱신
- `GET /api/trades?userId=1`: 사용자 거래내역 조회
- `PATCH /api/trades/{tradeId}` / `DELETE /api/trades/{tradeId}`: 거래 수정/삭제 후 해당 포지션 재계산

### 2. Dashboard & Analysis
- `GET /api/assets/summary`: 전체 자산 합계, 평가 손익 요약
- `GET /api/assets/by-account`: 계좌별 자산 비중
- `GET /api/assets/by-country`, `/by-type`, `/by-sector`, `/by-strategy`: 분류별 비중 통계
- `GET /api/dividends/summary`: 연간 예상 배당금 및 배당률 요약
- `GET /api/dividends/monthly`: 월별 배당 데이터
- `POST /api/dividends/payments/generate`: DividendEvent 기준 예상 DividendPayment 생성

### 3. Asset Management
- `GET /api/accounts`: 계좌 목록 조회
- `POST /api/holdings`: 보유 종목 직접 등록. 초기 보정용으로 유지하며 핵심 흐름은 거래내역 기반입니다.
- `GET /api/securities`: 종목 목록 조회
- `POST /api/security-classifications`: 국내 상장 해외 ETF 분석 필드를 포함한 종목 분류 등록

### 4. History & Market Price
- `GET /api/snapshots/timeline`: 기간별 자산 가치 변동 추이
- `POST /api/snapshots`: 현재 Holding 기준 스냅샷 저장
- `POST /api/market-prices`: 가격 이력 수동 등록
- `GET /api/market-prices/latest/security/{securityItemId}`: 종목 최신 가격 조회
- `POST /api/market-prices/refresh`: 실제 외부 연동 전 Stub provider 기반 갱신

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
- `TradeTransaction` 등록 시 `Holding`을 자동 생성/갱신합니다.
- 배당 지급 예정 생성은 `DividendEvent.recordDate` 기준 보유수량을 거래내역에서 계산합니다.

## 실행 및 검증 (Run & Verify)

### 로컬 실행
```bash
cd backend
./gradlew bootRun
```
- API Base: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console/` (local JDBC URL: `jdbc:h2:mem:assetmap-local`)

### Profile 구성
- `application.properties`: 공통 설정만 관리하며 기본 profile은 `local`입니다.
- `application-local.properties`: 로컬 단일 개발용 H2, H2 console, optional local seed 로딩을 사용합니다.
- `application-dev.properties`: 공유 개발 환경용이며 DB/JPA 설정을 환경변수로 override할 수 있습니다.
- `application-prod.properties`: 운영 환경용이며 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`을 환경변수로 주입해야 합니다. H2 console과 SQL seed는 비활성화합니다.

### 로컬 Seed 데이터
- 공통 설정은 SQL seed를 로드하지 않습니다.
- `local` profile은 Git에서 제외된 `data.local.sql`/`seed.local.sql`만 optional로 로드합니다.
- 로컬 개발 데이터는 `backend/src/main/resources/db/data.example.sql`을 `data.local.sql`로 복사해 작성합니다.
- `data.local.sql`, `seed.local.sql`, `*.local.csv`, `*.local.json`, `backend/src/main/resources/seed/local/**`는 Git 추적 대상에서 제외합니다.
- 로컬 seed를 사용할 때만 `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`으로 실행합니다.

### 테스트 수행
```bash
./gradlew test
```
- JUnit 5 기반의 단위 테스트 및 `MockMvc`를 활용한 API 테스트가 포함되어 있습니다.
