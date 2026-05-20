# Work Log

## 2026-05-16

### 프로젝트 뼈대 생성

- `asset-map/backend` 생성
- Spring Boot 3.5.6, Java 17, Gradle 설정
- backend 의존성 추가: Spring Web, Spring Data JPA, Validation, H2 Database, Lombok
- `asset-map/frontend` 생성
- Next.js, TypeScript, Tailwind CSS 설정
- frontend 의존성 추가: Recharts, TanStack React Query, React Hook Form, Zod
- 기본 홈 화면을 Asset Map용 단순 화면으로 정리
- 루트 `README.md` 작성
- backend `./gradlew test`, `./gradlew build` 성공 확인
- backend `./gradlew bootRun`으로 8080 실행 확인
- frontend `npm run build` 성공 확인
- frontend `npm run dev`로 3000 실행 확인

### Git 초기화 및 원격 push

- `asset-map` 루트에서 `git init` 실행
- 루트 `.gitignore` 추가
- `origin`을 `git@github.com-private:heoGom/asset-map.git`로 설정
- 첫 커밋 생성: `chore: initialize asset-map project`
- `git push -u origin main` 성공

### 프로젝트 내부 문서화

- 저장소 내부 `docs/` 디렉터리 추가
- 프로젝트 컨텍스트, backend, frontend 문서를 내부로 복제 및 정리
- 요구사항, 프롬프트, 의사결정, 작업 일지 문서 추가

### 독립 작업 루트 문서화

- 저장소 루트 `AGENTS.md` 추가
- 상위 워크스페이스의 공통 규칙 Markdown을 `docs/common/`으로 가져옴
- Asset Map 작업은 내부 `AGENTS.md`와 `docs/common/`을 우선하도록 문서 정리

### 문서 구조화

- `docs/project/` 추가: 프로젝트 컨텍스트와 요구사항 관리
- `docs/architecture/` 추가: backend/frontend 구현 정보 관리
- `docs/history/` 추가: 프롬프트, 의사결정, 작업 일지 관리
- `AGENTS.md`와 문서 내부 링크를 새 구조에 맞게 수정

### Backend 공통 구조 추가

- `ApiResponse`, `ErrorCode`, `BusinessException`, `GlobalExceptionHandler` 추가
- `BaseEntity`, `JpaAuditingConfig`, `GET /api/health` 추가
- H2 콘솔 및 JPA 설정 보강

### Backend 핵심 도메인 및 대시보드 구현

- Account, SecurityItem, SecurityClassification, Holding CRUD 추가
- 현재 보유 기준 자산 대시보드 API 추가
- HoldingSnapshot 저장 및 기간별 자산 변화 API 추가
- DividendEvent, DividendPayment CRUD 추가
- 배당 요약, 월별/연도별/종목별/성장률 API 추가
- `./gradlew test`, `./gradlew build`, `./gradlew bootRun` 및 주요 curl 검증 완료

### Frontend /assets 자산 대시보드 구현

- `frontend/src/app/assets/page.tsx` 생성: 총 자산 요약, 비중 차트, 보유 종목 목록을 포함한 대시보드 화면 구현
- `frontend/src/components/dashboard/` 내 공통 컴포넌트 추가: `SummaryCard`, `RatioChart` (Recharts 기반), `HoldingTable`
- `frontend/src/lib/api/` 내 API 클라이언트 추가: 자산 통계 및 보유 현황 조회를 위한 `assets.ts`, `holdings.ts`
- `frontend/src/lib/mock/` 내 테스트용 Mock 데이터 분리
- `frontend/src/lib/query-provider.tsx` 추가 및 `layout.tsx` 설정으로 TanStack Query 환경 구축
- Tailwind CSS를 활용한 반응형 카드형 UI 적용
- Backend API 연동 및 실패 시 Mock 데이터로의 부드러운 폴백 처리

### 자산 타임라인 및 배당 대시보드 고도화

- `frontend/src/components/dashboard/TimelineChart.tsx`: 자산 성장 추이를 보여주는 Line Chart 구현 (Recharts)
- `frontend/src/app/assets/page.tsx`: 자산 대시보드에 타임라인 차트 통합
- `frontend/src/app/dividends/page.tsx`: 배당 대시보드 신규 구현 (요약 카드, 월별 배당 차트, 종목별 배당 상세 테이블)
- `frontend/src/lib/api/dividends.ts`: 배당 관련 API 클라이언트 정의
- `frontend/src/lib/mock/dividends.ts`: 배당 테스트용 Mock 데이터 구축
- 홈 화면 네비게이션을 자산/배당 대시보드로 세분화

### 계좌 관리 UI 구현 및 레이아웃 표준화

- `frontend/src/app/accounts/page.tsx`: 계좌 목록 조회 및 신규 계좌 등록/삭제 기능 구현
- `frontend/src/lib/api/accounts.ts`: 계좌 CRUD를 위한 API 클라이언트 추가
- `frontend/src/lib/mock/accounts.ts`: 계좌 테스트용 Mock 데이터 구축
- TanStack Query를 활용한 실시간 상태 동기화 구현
- 공통 `Header`, `Footer` 컴포넌트 생성 및 `RootLayout` 적용으로 전 페이지 네비게이션 표준화

### 문서 정합성 보강

- `docs/project/api-test-scenarios.md` 추가
- `docs/project/README.md`와 `docs/project/project-context.md`에 수동 검증 순서 연결

### 거래 기반 포트폴리오 데이터 흐름 추가

- `TradeTransaction` 기반 자산 원천 데이터 구조 추가
- 거래 등록/수정/삭제 시 `Holding` 자동 재계산
- 배당 기준일 보유수량 계산 및 `DividendEvent` 기반 예상 `DividendPayment` 생성 API 추가
- `MarketPrice` 가격 이력 구조와 Stub provider 추가
- 국내 상장 해외 ETF 분석을 위한 분류 필드 보강
- 대시보드 mock fallback이 실제 데이터 문제를 가리지 않도록 최소 수정

### Frontend 거래 입력 흐름 전환

- `/assets`의 입력 흐름을 Holding 직접 등록에서 TradeTransaction 등록으로 전환
- 거래 입력 폼과 거래내역 목록 UI 추가
- 거래 등록 성공 후 거래 목록, Holding, 자산 대시보드 관련 쿼리 갱신
- 보유 현황은 Holding API 조회 전용으로 유지하고 empty state 추가

### 거래 입력 폼 계좌/종목 옵션 로딩 수정

- `GET /api/accounts`, `GET /api/securities`의 `ApiResponse.data` 배열을 거래 입력 폼 select 옵션으로 사용하도록 확인
- 프론트엔드 `ApiResponse` 타입을 실제 백엔드 응답 구조(`success`, `code`, `message`, `data`)와 일치하도록 수정
- 계좌/종목 데이터가 없을 때 empty 안내 문구를 표시하고 거래 등록 버튼을 비활성화

### JWT 기반 인증 흐름 추가

- BCrypt 비밀번호 암호화와 JWT 기반 회원가입/로그인, 현재 사용자 조회 API 추가
- Account와 TradeTransaction API를 로그인 사용자 기준으로 전환하고 타 사용자 계좌 거래를 차단
- frontend 로그인/회원가입 화면, AuthProvider, Authorization 헤더 처리를 추가하고 개발용 userId 선택 흐름 제거
- 자산/보유/배당 조회 화면이 인증된 사용자 컨텍스트에서 동작하도록 최소 보정

### 로컬 Seed 데이터 Git 제외

- Git 추적 중이던 `backend/src/main/resources/db/data.sql` seed 파일을 제거
- 기본 profile에서는 SQL seed를 로드하지 않고, `local` profile에서만 ignored `data.local.sql`/`seed.local.sql`을 선택적으로 로드하도록 변경
- 예시 템플릿 `data.example.sql`, `seed.example.json`과 로컬 seed 작성 방법을 문서화

### Backend Profile 설정 정리

- 공통 `application.properties`에서 환경별 설정을 분리하고 기본 profile을 `local`로 지정
- `application-local.properties`, `application-dev.properties`, `application-prod.properties`로 DB, H2 console, seed, logging, JWT secret 설정을 분리
- 운영 profile은 DB 접속 정보와 JWT secret을 환경변수에서 주입받도록 정리

## 2026-05-17

### 자산/배당 대시보드 데이터 흐름 안정화

- Git 제외 대상인 `data.local.sql`에 구조 검증용 local minimal seed를 정리했다.
- 자산/배당 API 응답 필드와 frontend mapping을 맞춰 NaN 표시를 방지하고, 수익률은 backend percent 값 기준으로 표시하도록 통일했다.
- 자산 스냅샷 기본 조회 범위를 보정해 local seed 기준 자산 성장 타임라인이 표시되도록 했다.
- 월별 배당금 현황은 지급일 기준 PAID 배당금 집계 값을 사용하고, 2025/2026년 local seed 데이터로 검증했다.
- `/accounts/{accountId}` 상세 페이지와 계좌별 보유 종목, 거래내역, 배당내역 조회 API를 추가했다.

### Theme/Language 토글 추가

- light/dark 테마 토글을 추가하고, 저장값이 없을 때만 OS `prefers-color-scheme` 기준으로 초기 테마를 결정하도록 했다.
- 한국어/영어 언어 토글과 내부 dictionary 기반 번역 구조를 추가했다.
- Header nav와 `/accounts`, `/assets`, `/dividends` 주요 제목/empty state 문구에 언어 전환을 우선 적용했다.
- 자산 비중 차트의 enum 카테고리 값을 현재 언어에 맞는 표시 라벨로 변환하도록 했다.

### 직접 입력 기반 데이터 관리 흐름 추가

- `/securities`에서 종목 마스터와 종목 분류를 직접 등록/수정할 수 있게 했다.
- `/assets`에 보유 종목 현재가 수동 입력 흐름을 추가하고, `MarketPrice` 저장 시 `Holding.currentPrice`를 갱신하도록 연결했다.
- `/dividends`에 배당/분배금 이벤트 직접 입력과 내 배당금 생성 흐름을 추가했다.
- 거래 입력 폼에서 종목이 없을 때 종목 등록 화면으로 이동할 수 있게 했다.

### 금융위원회 주식배당정보 import 추가

- 공공데이터포털 금융위원회 주식배당정보 API를 `PUBLIC_DATA_STOCK_DIVIDEND_SERVICE_KEY` 환경변수 기반으로 호출하도록 추가했다.
- 현재 사용자 보유/거래 내역에 등장한 국내 `STOCK` 종목만 대상으로 2020년 이후 배당기준일 데이터를 import한다.
- import된 데이터는 `DividendEvent`로 저장하고 기존 `DividendPayment` 생성 서비스를 재사용해 계좌별 내 배당금을 자동 생성한다.
- ETF 분배금은 자동 import 대상에서 제외하고 기존 수동 입력 흐름을 유지한다.

### 금융위원회 주식배당정보 import 보강

- serviceKey 설정값이 Encoding/Decoding 어느 형태든 정상 호출되도록 normalize 처리했다.
- 우선주 검색어 후보(`삼성전자우` → `삼성전자`, `현대차3우B` → `현대자동차`)와 보통주/우선주 매칭을 보강했다.
- 현재 사용자 Holding/TradeTransaction에 등장한 국내 `STOCK` 종목만 대상으로 2020년 이후 배당 이벤트를 import한다.
- `DividendEvent` 저장 후 기존 생성 서비스를 재사용해 `DividendPayment`를 자동 생성한다.
- ETF 분배금은 이번 자동 import 대상에서 제외하고 수동 입력 대상으로 유지한다.
- `DividendEvent` 저장/`DividendPayment` 생성을 별도 트랜잭션으로 분리해 개별 이벤트 저장 실패가 전체 import 세션을 오염시키지 않도록 보강했다.

### Local Docker MySQL 개발 DB 정리

- local profile을 Docker MySQL(`localhost:23306/asset_map`) 기준으로 정리하고 test profile은 H2를 유지하도록 분리했다.
- local SQL 자동 seed를 끄고, synthetic minimal seed는 명시 실행용 `db/local/seed-minimal.sql`로 분리했다.
- DB 초기화/백업/복원 스크립트를 `scripts/`에 추가하고 `backups/*.sql`은 Git 제외 대상으로 관리한다.
- 실제 투자 full seed와 mock fallback은 사용하지 않고, 데이터가 없으면 empty state를 보여주는 정책을 문서화했다.

### KRX 승인 대기용 데이터 동기화 기반 구조 추가

- `DataSyncStatus` 기반으로 syncType/source/targetKey별 실행 상태와 마지막 성공 일자를 관리하는 구조를 추가했다.
- KRX 종목 마스터/시세 Provider 인터페이스와 Stub 구현을 준비했으며, Stub은 빈 리스트만 반환하고 실제 외부 API를 호출하지 않는다.
- `SecurityItem`은 ticker 기준 upsert 구조를 준비했고, 종목 마스터는 KRX 승인 후 전체 수집 대상으로 연결할 예정이다.
- `MarketPrice`는 ticker 매칭 후 `securityItemId + priceDate + source` 기준 upsert 구조를 준비했고, 시세 저장 대상은 보유/거래/관심 종목 중심으로 제한할 예정이다.
- 관리자용 `/api/admin/sync/status`, `/api/admin/sync/security-master`, `/api/admin/sync/market-prices` 기본 endpoint를 추가했다.

### KRX 종목 마스터 수집 기능 추가

- KRX 유가증권/코스닥 종목기본정보 API를 `KRX_API_KEY` 환경변수 기반 `AUTH_KEY` header와 JSON body로 호출하도록 구현했다.
- 응답 `OutBlock_1`의 표준코드, 단축코드, 종목명, 시장구분, 상장일을 내부 import DTO로 변환하고 `SecurityItem`은 ticker 기준으로 upsert한다.
- 종목 마스터는 전체 수집 대상이며, `SecurityItem`에는 장기 식별용 `isinCode`를 추가했다.
- `DataSyncStatus`는 `SECURITY_MASTER`/`KRX`/`ALL` 기준으로 실행 상태와 당일 성공 여부를 관리하고, `force=true`면 재실행한다.
- 이번 단계에서는 KRX 시세, ETF 일별매매정보, `MarketPrice`, `Holding.currentPrice`, `HoldingSnapshot` 저장은 연결하지 않았다.
- 시세 수집은 다음 단계에서 현재 보유/거래/관심 종목만 DB에 저장하고 화면은 DB만 조회하는 구조로 유지한다.

### 데이터 동기화 정책과 KRX 시세 1차 구현 정리

- `DataSyncPolicyService`를 추가해 `DataSyncStatus`, force 여부, 로컬 DB 보유 여부를 함께 보고 sync 실행/skip을 판단하게 했다.
- local profile에서는 서버 시작 후 종목 마스터가 비어 있거나 당일 최신화가 필요할 때 KRX 종목 마스터 동기화를 조건부 실행하고, 실패해도 앱 부팅은 유지한다.
- KRX 유가증권/코스닥/ETF 일별매매정보 provider를 추가하되, 전체 응답 중 현재 사용자 Holding/TradeTransaction 종목만 `MarketPrice`로 upsert한다.
- KRX 시세 저장 후 최신 기준일이면 기존 흐름으로 `Holding.currentPrice`를 갱신하고, HoldingSnapshot 자동 생성은 별도 정책 확정 전까지 TODO로 유지한다.
- local minimal seed는 실제 KRX로 수집 가능한 STOCK 종목 마스터를 직접 넣지 않고, KRX sync 이후 실제 ticker 기준으로 거래/보유 샘플을 연결하도록 정리했다.
- 시세는 내 종목만 저장, 배당은 내 STOCK 종목만 2020년 이후 수집, ETF 분배금은 수동 입력 유지 정책을 문서화했다.

### 외부 데이터 startup/backfill/schedule 동기화 보강

- `@EnableScheduling`과 `ExternalDataStartupSyncRunner`, `ExternalDataSyncScheduler`를 추가해 startup 확인과 정기 스케줄이 동일한 `AdminSyncService` 정책을 호출하도록 정리했다.
- local profile은 `app.sync.enabled=true`와 세부 on-startup/cron 설정으로 종목마스터, 시세, 배당 동기화를 켜고, test profile은 `app.sync.enabled=false`로 외부 API 호출을 차단한다.
- 수동 API는 `/api/admin/sync/security-master`, `/api/admin/sync/market-prices`, `/api/admin/sync/stock-dividends`로 유지하며 `force=true`, `basDd`/`priceDate`, `fromYear`/`toYear`를 통한 강제 재동기화 용도로 동작한다.
- 시세 대상은 `Holding`이 아니라 `TradeTransaction`에 등장한 `STOCK`/`ETF` 기준으로 변경했다. KRX API가 전체 시장 데이터를 반환해도 저장은 거래내역 대상 ticker만 수행한다.
- 시세 backfill은 기존 KRX `MarketPrice`의 최대 `priceDate` 다음 날부터 필요한 날짜만 시도하고, 데이터가 없으면 기본 30일/최대 60일 제한 안에서 보강한다.
- 배당 대상은 `TradeTransaction`에 등장한 국내 `STOCK` 기준으로 변경했다. 최초 또는 이벤트가 없을 때는 2020년부터, 이후에는 최근 2개 연도를 재확인하며 ETF 분배금은 수동 입력으로 유지한다.
- 배당 이벤트 중복 발견 시에도 거래 사용자별 `DividendPayment` 생성을 재시도해 기존 이벤트에 대해 누락된 payment가 보강되도록 했다.
- 날짜별 시세 상태는 `TRADED_SECURITIES_YYYYMMDD`, 전체 시세 상태는 `TRADED_SECURITIES`, 배당 상태는 `TRADED_STOCK_SECURITIES`로 `DataSyncStatus`에 기록한다.
- local seed는 실제 KRX STOCK master를 포함하지 않고, 실제 공개 ticker를 참조하는 최소 사용자/계좌/거래 샘플 정책을 유지한다.

### 종목 선택 자동완성 UI 적용

- 거래 입력, 현재가 직접 입력, 배당/분배금 이벤트 입력, 종목 분류 입력의 종목 선택 UI를 검색 가능한 공통 `SecurityCombobox`로 교체했다.
- 종목명, ticker, 시장, 통화, 유형 텍스트를 기준으로 클라이언트 필터링하며 대량 종목 목록은 최대 표시 개수로 제한해 긴 select 렌더링을 피한다.
- 거래 입력 submit 조건에 종목/계좌 선택 여부를 명시적으로 포함해 기존 select required 역할을 보완했다.

### 앱 내비게이션과 기본 UI 재정비

- 종목 마스터는 외부 데이터 동기화 기준으로 관리되므로 1차 내비게이션에서 제외하고 Header의 `관리` 메뉴와 홈의 운영 관리 영역으로 이동했다.
- light/dark와 KO/EN 전환을 단일 액션 버튼 대신 현재 상태가 보이는 segmented toggle 형태로 바꿨다.
- 홈 화면을 마케팅 랜딩 페이지에서 자산/배당/계좌 작업으로 바로 진입하는 앱 허브 화면으로 재구성했다.
- Footer를 더 낮은 시각 우선순위의 compact app footer로 정리했다.

### 외부 데이터 sync P0 정합성 보강

- 시세 sync의 30일/60일 대상 기간 제한을 제거하고, `TradeTransaction` 종목별 최초 거래일부터 실제 KRX `MarketPrice` 누락 여부를 날짜별로 확인하도록 변경했다.
- `DataSyncStatus` 날짜 성공 기록이 있더라도 해당 날짜의 일부 종목 시세가 비어 있으면 누락 종목만 다시 가져오도록 했다.
- 배당 sync는 오늘 성공 기록만으로 skip하지 않고, 국내 `STOCK` 거래 종목의 최초 거래연도와 `DividendEvent` 실제 데이터를 함께 보고 과거 누락분을 복구할 수 있게 했다.
- `MarketPrice`, `DividendEvent`, `DividendPayment`에 자연키 기준 unique constraint를 추가해 재시도/동시 실행 시 중복 저장을 DB 레벨에서도 방어한다.

### 외부 데이터 sync P1 안정화

- `NO_DATA`를 영구 skip 신호가 아니라 `app.sync.no-data-recheck-days` TTL이 있는 checkpoint로 변경했다. 정상 API 응답의 빈 데이터만 `NO_DATA`로 기록하고, API/인증/응답 코드/파싱 오류는 `FAILED`로 기록해 다음 실행에서 재시도한다.
- 시세 sync는 날짜별 `TRADED_SECURITIES_YYYYMMDD` checkpoint 단위로 외부 호출, 저장, 상태 기록을 분리해 일부 날짜 실패 시 이전 날짜 성공분이 유지되도록 보강했다.
- 배당 sync는 국내 `STOCK` 거래 종목의 종목+연도 단위 `STOCK_DIVIDEND_{SECURITY_ID}_{YEAR}` checkpoint를 남기고, 무배당 연도는 `NO_DATA`, 실패 연도는 `FAILED`로 구분한다.
- startup, scheduler, manual force 경로는 계속 `AdminSyncService`의 동일 정책을 호출하며, ETF 분배금 자동화는 추가하지 않고 수동 입력 정책을 유지한다.

### 외부 데이터 sync 상태 조회 상세화

- 기존 `/api/admin/sync/status` 리스트 조회는 유지하고, `/api/admin/sync/status/detail`을 추가해 운영 중 startup/scheduler/manual sync 결과를 한 번에 확인할 수 있게 했다.
- 상세 응답은 종목마스터 마지막 성공/실패 시각, 시세 대상 종목 수와 `MarketPrice` 기준 누락/대기 날짜, 배당 대상 종목 수와 `DividendEvent` 기준 누락 또는 재확인 대상 종목+연도 수를 반환한다.
- FAILED checkpoint와 fresh/expired `NO_DATA`를 구분해 최근 실패 날짜 및 종목+연도 targetKey를 관리자 화면에서 바로 표시할 수 있게 했다.
