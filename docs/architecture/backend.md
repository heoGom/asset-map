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
| **Database** | MySQL 8.0, H2 | Local은 Docker MySQL, Test는 H2 |
| **ORM** | Spring Data JPA | 도메인 모델 영속화 |
| **Utilities** | Lombok, Validation | 코드 생산성 및 데이터 검증 |

## 도메인 모델 (Domain Model)

| 엔티티 | 설명 | 주요 필드 |
| --- | --- | --- |
| `Account` | 증권/연금/현금 계좌 정보 | `userId`, `name`, `brokerName`, `accountType`, `currency` |
| `SecurityItem` | 종목 마스터 정보 | `ticker`, `isinCode`, `name`, `market`, `country`, `securityType` |
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

### 5. Admin Sync
- `GET /api/admin/sync/status`: syncType/source/targetKey별 동기화 상태 조회
- `GET /api/admin/sync/status/detail`: 운영 확인용 상세 상태 조회. 종목마스터 마지막 성공/실패 시각, 시세 대상/누락/대기 날짜/FAILED/NO_DATA 집계, 배당 대상/이벤트 보유/재확인 대상/FAILED/NO_DATA 집계를 반환합니다.
- `POST /api/admin/sync/security-master`: KRX 유가증권/코스닥 종목기본정보를 호출해 전체 종목 마스터를 `SecurityItem`에 ticker 기준 upsert. `force=false`면 당일 성공 기록이 있을 때 skip하고, DB 종목 마스터가 비어 있으면 실행합니다.
- `POST /api/admin/sync/market-prices`: KRX 유가증권/코스닥/ETF 일별매매정보를 날짜별로 호출하되, `TradeTransaction`에 등장한 `STOCK`/`ETF` 종목만 `MarketPrice`에 저장합니다. `force=true` 또는 `priceDate`/`basDd` 지정 시 수동 재동기화 용도로 동작합니다.
- `POST /api/admin/sync/stock-dividends`: 금융위원회 주식배당정보를 `TradeTransaction`에 등장한 국내 `STOCK` 종목 기준으로 import합니다. ETF 분배금은 자동 대상이 아니며 수동 입력을 유지합니다.

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

### 데이터 동기화 구조
- `DataSyncStatus`는 `syncType`, `source`, `targetKey` 조합으로 동기화 실행 상태와 마지막 성공 일자를 저장합니다.
- `DataSyncPolicyService`는 `force`, `DataSyncStatus`, 로컬 DB 보유 여부를 함께 보고 실행/skip을 판단합니다.
- `local` profile은 `app.sync.enabled=true`, `app.sync.on-startup.enabled=true`일 때 서버 시작 후 종목마스터, 시세, 배당 동기화 필요 여부를 순서대로 확인합니다. 각 단계는 독립 `try/catch`로 실행되어 실패해도 애플리케이션 부팅을 막지 않습니다.
- `test` profile은 `app.sync.enabled=false`를 유지하므로 startup/scheduled 외부 API 호출이 실행되지 않습니다.
- `ExternalDataSyncScheduler`는 `@Scheduled`로 같은 `AdminSyncService` 정책을 호출합니다. 기본 local 스케줄은 종목마스터 매일 07:10, 시세 평일 18:30, 배당 매일 08:00입니다.
- KRX 종목 마스터는 `KRX_API_KEY` 설정값을 `AUTH_KEY` header로 전달하고, `{"basDd":"YYYYMMDD"}` JSON body로 유가증권/코스닥 종목기본정보를 호출합니다. 실제 키 값은 Git에 포함하지 않습니다.
- 종목 마스터는 전체 수집 대상으로 보고 `ticker` 기준으로 `SecurityItem`을 upsert합니다. KRX `ISU_CD`는 `isinCode`에 저장합니다.
- 시세 대상은 `Holding`이 아니라 `TradeTransaction`에 한 번이라도 등장한 `STOCK`/`ETF` 종목입니다. 현재 보유하지 않더라도 과거 거래 종목이면 backfill 대상에 포함됩니다.
- KRX 시세 API는 `basDd` 기준 전체 시장 응답을 내려주지만, 저장은 거래내역 대상 ticker만 수행합니다. 중복 기준은 `securityItemId + priceDate + source`이며, 최신 가격이면 `Holding.currentPrice`를 갱신합니다.
- 시세 backfill은 `TradeTransaction`에 등장한 각 `STOCK`/`ETF` 종목의 최초 거래일부터 오늘까지 실제 KRX `MarketPrice` 존재 여부를 종목별/날짜별로 확인합니다. `DataSyncStatus` 성공 기록만으로 skip하지 않고, 특정 날짜에 일부 종목만 저장되어 있으면 누락 종목만 재시도합니다. `app.sync.market-prices.max-backfill-days`는 전체 대상 기간 제한이 아니라 1회 실행에서 처리할 날짜 chunk 크기로만 사용합니다.
- 배당 API는 `TradeTransaction`에 등장한 국내 `STOCK` 종목만 대상으로 합니다. 오늘 성공 기록만으로 skip하지 않고, 종목별 최초 거래연도와 `DividendEvent` 실제 존재 여부를 함께 확인해 과거 누락분이 있으면 기본 시작연도부터 현재 연도까지 다시 확인합니다. 과거 구간이 채워져 있으면 최근 `app.sync.stock-dividends.recheck-years` 연도를 재확인합니다. ETF 분배금은 자동 대상이 아니며 수동 입력을 유지합니다.
- `NO_DATA`는 정상 API 응답에서 대상 데이터가 없을 때만 기록하며, `app.sync.no-data-recheck-days` 기간 동안 반복 호출을 막는 checkpoint로만 사용합니다. TTL이 지나면 다시 확인 대상에 포함하고, HTTP/API/인증/파싱 오류는 `FAILED`로 기록해 다음 실행에서 재시도합니다.
- 시세 sync는 날짜별 `TRADED_SECURITIES_YYYYMMDD`, 배당 sync는 종목+연도별 `STOCK_DIVIDEND_{SECURITY_ID}_{YEAR}` checkpoint를 기록합니다. 날짜 또는 종목+연도 단위 저장/상태 기록을 독립 처리해 중간 실패가 이전 성공분을 rollback하지 않도록 합니다.
- 상세 sync status는 `DataSyncStatus` 실행 상태와 실제 `MarketPrice`/`DividendEvent` DB 존재 여부를 함께 집계합니다. `NO_DATA`는 fresh/expired로 나누고, 최근 실패 항목은 날짜 또는 종목+연도 targetKey 기준으로 표시합니다.
- 배당 이벤트 저장 후 거래 사용자별 `DividendPayment` 생성을 재시도합니다. 이미 이벤트나 payment가 있으면 중복 저장하지 않습니다.
- ETF 분배금은 자동 API 대상이 아니며 수동 입력을 유지합니다.

## 실행 및 검증 (Run & Verify)

### 로컬 실행
```bash
docker compose up -d
cd backend
./gradlew bootRun
```
- API Base: `http://localhost:8080`
- Local DB: `jdbc:mysql://localhost:23306/asset_map?serverTimezone=Asia/Seoul&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true`
- Local secrets: `backend/.local-secrets.properties`에 `KRX_API_KEY`, 공공데이터 service key, JWT secret 등 로컬 인증값을 모아 둡니다. 실제 파일은 Git 제외 대상이며, 템플릿은 `backend/.local-secrets.properties.example`입니다.

### Profile 구성
- `application.properties`: 공통 설정만 관리하며 기본 profile은 `local`입니다.
- `application-local.properties`: Docker MySQL을 사용합니다. `ddl-auto=update`, SQL init 비활성화, Docker volume 기반 데이터 유지, 외부 데이터 startup/scheduled sync 활성화를 기본으로 합니다.
- `application-test.properties`: 테스트 전용 H2 인메모리 DB를 사용합니다. `ddl-auto=create-drop`, SQL init 비활성화, `app.sync.enabled=false`를 유지합니다.
- `application-dev.properties`: 공유 개발 환경용이며 DB/JPA 설정을 환경변수로 override할 수 있습니다.
- `application-prod.properties`: 운영 환경용이며 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`을 환경변수로 주입해야 합니다. H2 console과 SQL seed는 비활성화합니다.

### 로컬 Seed 데이터
- 공통 설정과 local profile은 SQL seed를 자동 로드하지 않습니다.
- Docker MySQL은 volume을 사용하므로 서버 재시작 후에도 데이터가 유지됩니다.
- 최초 개발 편의용 synthetic minimal seed는 `backend/src/main/resources/db/local/seed-minimal.sql`에 두고, backend를 한 번 실행해 테이블과 KRX 종목 마스터를 만든 뒤 명시적으로만 적용합니다.
- 실제 KRX로 수집 가능한 STOCK 종목 마스터는 local seed에 넣지 않습니다. local seed는 KRX sync 후 ticker 기준으로 `SecurityItem`을 참조하고, ETF 분배금은 자동 연동하지 않으므로 필요한 최소 ETF 샘플만 실제 공개 ticker/name과 맞춰 유지합니다.
- 실제 투자 full seed, API key, 민감정보가 들어간 파일은 Git 추적 대상이 아닙니다.
- 화면은 데이터가 없을 때 mock fallback이 아니라 empty state를 보여주는 정책을 유지합니다.

### Local DB 운영 스크립트
```bash
./scripts/reset-local-db.sh
./scripts/backup-local-db.sh before-work
./scripts/restore-local-db.sh backups/asset_map_before-work_YYYYMMDD-HHMMSS.sql
docker compose down
docker compose down -v
```
- `reset-local-db.sh`: Docker volume을 삭제하고 깨끗한 MySQL을 다시 실행합니다. seed는 자동 적용하지 않습니다.
- `backup-local-db.sh`: 현재 `asset_map` DB를 `backups/` 아래 SQL dump로 저장합니다.
- `restore-local-db.sh`: 지정한 SQL dump 파일로 `asset_map` DB를 drop/create 후 복원합니다.
- Minimal seed가 필요하면 backend를 한 번 실행해 JPA가 테이블을 만든 뒤 다음처럼 명시 적용합니다.
```bash
docker exec -i asset-map-mysql mysql -uassetmap -passetmap asset_map < backend/src/main/resources/db/local/seed-minimal.sql
```

### 테스트 수행
```bash
./gradlew test
```
- JUnit 5 기반의 단위 테스트 및 `MockMvc`를 활용한 API 테스트가 포함되어 있습니다.
