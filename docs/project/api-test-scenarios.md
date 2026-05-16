# Asset Map API Test Scenarios

이 문서는 **Asset Map** 백엔드 API의 동작을 수동으로 점검하기 위한 테스트 시나리오입니다. 각 기능이 비즈니스 요구사항에 맞게 올바르게 동작하는지 확인하는 데 사용됩니다.

---

## 🚀 실행 환경 및 준비

### 실행 환경
- **Backend API Base**: `http://localhost:8080`
- **H2 Console**: `http://localhost:8080/h2-console/`
- **JDBC URL**: `jdbc:h2:mem:assetdb`

### 사전 준비
- 백엔드 서버가 로컬에서 실행 중이어야 합니다 (`./gradlew bootRun`).
- 현재 단계에서는 별도의 인증 헤더가 필요하지 않습니다.

---

## 📋 테스트 시나리오 목록

### 1. 계좌 및 종목 (Core Assets)

| 구분 | Method | URL | 검증 포인트 |
| --- | --- | --- | --- |
| 계좌 등록 성공 | `POST` | `/api/accounts` | 201 Created, 생성된 계좌 ID 확인 |
| 계좌 목록 조회 | `GET` | `/api/accounts` | 등록된 계좌 리스트 정상 반환 여부 |
| 종목 정보 등록 | `POST` | `/api/securities` | 티커, 섹터 등 마스터 정보 저장 확인 |
| 종목 검색 | `GET` | `/api/securities/search?keyword=AAPL` | 티커 또는 이름으로 검색 결과 확인 |

### 2. 보유 현황 및 대시보드 (Holdings & Dashboard)

| 구분 | Method | URL | 검증 포인트 |
| --- | --- | --- | --- |
| 보유 종목 등록 | `POST` | `/api/holdings` | 특정 계좌에 종목 추가 (수량, 평단가) |
| 전체 자산 요약 | `GET` | `/api/dashboard/summary` | 총 자산 가치 및 평가 손익 계산 검증 |
| 자산 비중 통계 | `GET` | `/api/dashboard/ratios` | 섹터별/국가별 비중 데이터(%) 확인 |
| 보유 목록 조회 | `GET` | `/api/holdings` | 계좌별 보유 종목 및 상세 정보 확인 |

### 3. 이력 및 배당 (History & Dividends)

| 구분 | Method | URL | 검증 포인트 |
| --- | --- | --- | --- |
| 현재 스냅샷 저장 | `POST` | `/api/snapshots/capture` | 현재 자산 상태 저장 및 성공 응답 |
| 자산 타임라인 조회 | `GET` | `/api/snapshots/timeline` | 날짜별 자산 가치 변동 데이터 확인 |
| 예상 배당 등록 | `POST` | `/api/dividends/events` | 종목별 배당락일 및 주당 배당금 저장 |
| 월별 배당 요약 | `GET` | `/api/dividends/monthly` | 특정 월의 예상 배당금 합계 확인 |

---

## 🧪 주요 요청 예시 (CURL)

### 1. 신규 계좌 등록
```bash
curl -X POST 'http://localhost:8080/api/accounts' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "메인 주식 계좌",
    "accountNumber": "123-456-789",
    "accountType": "STOCK",
    "brokerName": "한국투자증권"
  }'
```

### 2. 보유 종목 추가 (AAPL 10주 매수)
```bash
curl -X POST 'http://localhost:8080/api/holdings' \
  -H 'Content-Type: application/json' \
  -d '{
    "accountId": 1,
    "securityItemId": 1,
    "quantity": 10,
    "averagePrice": 185.50
  }'
```

### 3. 자산 요약 대시보드 조회
```bash
curl 'http://localhost:8080/api/dashboard/summary'
```

---

## ⚖️ 데이터 검증 체크리스트
- [ ] 금액 계산 시 소수점 오차가 발생하지 않는가? (`BigDecimal` 확인)
- [ ] 존재하지 않는 ID로 요청 시 명확한 에러 코드(`RESOURCE_NOT_FOUND`)가 반환되는가?
- [ ] 필수 파라미터 누락 시 `400 Bad Request`가 반환되는가?
- [ ] 자산 비중 합계가 약 100%에 근접하는가?
