# API Test Scenarios

## 목적

Asset Map 백엔드의 핵심 API를 수동으로 점검할 때 사용하는 시나리오다.

## 공통 준비

- backend 실행: `cd backend && SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`
- H2 콘솔: `http://localhost:8080/h2-console/`
- 공통 응답 형식: `ApiResponse<T>`

## 검증 순서

1. `GET /api/health`
2. `POST /api/auth/signup`
3. `POST /api/auth/login`
4. `GET /api/users/me`
5. `POST /api/accounts`
6. `POST /api/securities`
7. `POST /api/security-classifications`
8. `GET /api/assets/summary`
9. `GET /api/snapshots/timeline?from=2026-01-01&to=2026-12-31`
10. `POST /api/dividends/events`
11. `GET /api/dividends/summary`

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
Authorization: Bearer <accessToken>

{
  "name": "Example Account",
  "brokerName": "Example Broker",
  "accountType": "GENERAL",
  "currency": "KRW",
  "memo": "local test only"
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
curl -H 'Authorization: Bearer <accessToken>' http://localhost:8080/api/dividends/summary
```

기대 결과:

- `expectedAnnualDividendKrw`
- `averageMonthlyDividendKrw`
- `portfolioDividendYield`
- `yieldOnCost`
- `currentYearReceivedDividendKrw`
- `totalReceivedDividendKrw`
