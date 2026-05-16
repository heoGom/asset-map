# Asset Map Backend

## 기술 스택

- Spring Boot 3.5.6
- Java 17
- Gradle
- Spring Web
- Spring Data JPA
- Validation
- H2 Database
- Lombok

## 현재 상태

현재 backend에는 다음이 구현되어 있습니다.

- 공통 구조
  - `ApiResponse`
  - `ErrorCode`
  - `BusinessException`
  - `GlobalExceptionHandler`
  - `BaseEntity`
  - `JpaAuditingConfig`
  - `GET /api/health`
- 도메인
  - `account`: 계좌 CRUD
  - `securityitem`: 종목 CRUD
  - `classification`: 종목 분류 CRUD
  - `holding`: 보유 종목 CRUD
  - `snapshot`: 보유 자산 스냅샷 저장 및 기간별 조회
  - `dividend`: 배당 이벤트/배당 지급 CRUD 및 배당 대시보드
  - `dashboard`: 현재 보유 기준 자산 대시보드

이번 단계에서는 Spring Security, JWT, 사용자 인증/인가, 실시간 시세/환율 자동화는 아직 포함하지 않습니다.

## 패키지 구조

```text
com.assetmap.backend
├── account
├── classification
├── common
├── config
├── dashboard
├── dividend
├── health
├── holding
├── securityitem
└── snapshot
```

## 주요 API 범위

- Health: `/api/health`
- Accounts: `/api/accounts`
- Securities: `/api/securities`
- Security Classifications: `/api/security-classifications`
- Holdings: `/api/holdings`
- Asset Dashboard: `/api/assets/*`
- Snapshots: `/api/snapshots/*`
- Dividend Events: `/api/dividends/events/*`
- Dividend Payments: `/api/dividends/payments/*`
- Dividend Dashboard: `/api/dividends/summary`, `/api/dividends/monthly`, `/api/dividends/yearly`, `/api/dividends/by-security`, `/api/dividends/growth`

## 실행 방법

```bash
cd backend
./gradlew bootRun
```

기본 실행 주소는 `http://localhost:8080`입니다.

H2 콘솔 주소는 `http://localhost:8080/h2-console/`입니다.

## 검증 방법

```bash
cd backend
./gradlew test
./gradlew build
./gradlew bootRun
```

검증 시 최소 확인 항목:

- `GET /api/health`
- Account/SecurityItem/Holding 생성
- `/api/assets/summary`
- `/api/snapshots/timeline`
- `/api/dividends/summary?userId=1`
