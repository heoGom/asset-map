# API Test Scenarios

## 목적

Asset Map 백엔드의 핵심 API를 수동으로 점검할 때 사용하는 시나리오다.

## 공통 준비

- backend 실행: `cd backend && ./gradlew bootRun`
- H2 콘솔: `http://localhost:8080/h2-console/`
- 공통 응답 형식: `ApiResponse<T>`

## 검증 순서

1. `GET /api/health`
2. `POST /api/accounts`
3. `POST /api/securities`
4. `POST /api/security-classifications`
5. `POST /api/holdings`
6. `GET /api/assets/summary`
7. `POST /api/snapshots`
8. `GET /api/snapshots/timeline?userId=1&from=2026-01-01&to=2026-12-31`
9. `POST /api/dividends/events`
10. `POST /api/dividends/payments`
11. `GET /api/dividends/summary?userId=1`

## 주요 요청 예시

### Health Check

```bash
curl http://localhost:8080/api/health
```

기대 결과:

- `success = true`
- `code = SUCCESS`
- `data.status = OK`

### Account 생성

```http
POST /api/accounts
Content-Type: application/json

{
  "userId": 1,
  "name": "미래에셋 ISA",
  "brokerName": "미래에셋증권",
  "accountType": "ISA",
  "currency": "KRW",
  "memo": "절세 계좌"
}
```

### Asset Summary 확인

```bash
curl http://localhost:8080/api/assets/summary
```

기대 결과:

- `totalInvestedAmount`
- `totalEvaluatedAmount`
- `totalProfitLoss`
- `totalProfitLossRate`
- `holdingCount`

### Dividend Summary 확인

```bash
curl 'http://localhost:8080/api/dividends/summary?userId=1'
```

기대 결과:

- `expectedAnnualDividendKrw`
- `averageMonthlyDividendKrw`
- `portfolioDividendYield`
- `yieldOnCost`
- `currentYearReceivedDividendKrw`
- `totalReceivedDividendKrw`
