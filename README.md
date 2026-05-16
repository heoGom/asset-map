# Asset Map

Spring Boot와 Next.js로 구현한 자산 및 배당 정보 시각화 풀스택 애플리케이션입니다. 개인의 금융 자산(계좌, 종목, 보유 현황)을 관리하고, 과거 자산 흐름과 미래 예상 배당금을 대시보드 형태로 제공하는 데 초점을 맞추고 있습니다.

이 프로젝트는 백엔드 도메인 모델링부터 프론트엔드 데이터 시각화까지 한 흐름으로 구성하며, 자산 현황을 직관적으로 파악할 수 있는 대시보드 기능을 핵심으로 합니다.

## Highlights

- 계좌 및 종목별 통합 자산 관리 시스템
- 자산 비중 및 계좌별 자산 분포 시각화 (Pie/Bar Chart)
- 과거 자산 흐름 추적을 위한 스냅샷 및 타임라인 기능
- 상세 배당 정보 관리 (월별, 연도별, 종목별 배당 요약 및 성장률)
- Next.js App Router와 TanStack Query 기반의 프론트엔드 아키텍처
- Spring Boot 3와 JPA를 활용한 도메인 주도 설계
- Backend와 Frontend를 하나의 Monorepo로 관리

## Tech Stack

| Area | Stack |
| --- | --- |
| Backend | Java 17, Spring Boot 3, Spring Data JPA, Gradle |
| Database | H2 (Local/Test) |
| Frontend | Next.js 16, React 19, TypeScript, Tailwind CSS 4 |
| Visualization | Recharts |
| Data Fetching | TanStack React Query |
| Form/Validation | React Hook Form, Zod |

## Architecture

```text
asset-map/
  backend/
    src/main/java/com/assetmap/backend/
      account/        # 계좌 관리 (증권사, 유형 등)
      securityitem/   # 종목 정보 (티커, 섹터, 국가 등)
      holding/        # 보유 현황 관리
      snapshot/       # 과거 자산 기록 및 타임라인
      dividend/       # 배당 일정 및 지급 내역 관리
      dashboard/      # 자산/배당 요약 및 통계 API
      classification/ # 종목 및 자산 분류 체계
      common/         # 응답, 예외, 엔티티 공통 처리
  frontend/
    src/
      app/            # App Router 기반 화면 구성
      components/     # UI 공통 및 도메인 컴포넌트
      hooks/          # 커스텀 훅 및 API 연동
      lib/            # 공통 유틸리티 및 설정
  docs/               # 프로젝트 기획 및 설계 문서
```

## Core Features

### Dashboard

- 전체 자산 요약 (총 자산, 평가 손익 등)
- 자산 구성 비중 시각화 (섹터별, 국가별, 자산군별)
- 과거 자산 흐름 타임라인 차트

### Asset Management

- 증권 계좌 및 개별 종목 관리
- 실시간 보유 수량 및 평단가 관리
- 계좌별 보유 종목 그룹화 및 자산 합계 조회

### Dividend Tracking

- 종목별 배당 주기 및 금액 관리
- 월별 예상 배당금 달력 및 차트 제공
- 연도별 배당 성장률 및 종목별 배당 기여도 분석

### Snapshot & History

- 주기적인 자산 현황 스냅샷 저장
- 특정 시점의 자산 상태 복구 및 비교 분석
- 자산 증감 이력 추적

## API Overview

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/dashboard/summary` | 전체 자산 요약 조회 |
| `GET` | `/api/dashboard/ratios` | 자산 비중 통계 조회 |
| `GET` | `/api/accounts` | 계좌 목록 및 상세 조회 |
| `POST` | `/api/holdings` | 보유 종목 추가/수정 |
| `GET` | `/api/snapshots/timeline` | 자산 추이 타임라인 조회 |
| `GET` | `/api/dividend/monthly` | 월별 배당 내역 조회 |
| `GET` | `/api/dividend/growth` | 배당 성장률 분석 조회 |
| `GET` | `/api/health` | 서버 상태 체크 |

## Getting Started

### Requirements

- macOS (추천)
- Java 17 이상
- Node.js 20 이상
- IDE: IntelliJ IDEA, VS Code

### Backend Local

```bash
cd backend
./gradlew bootRun
```

Backend runs at: `http://localhost:8080`

### Local Seed Data

Git에 실제 투자 데이터나 로컬 seed 파일을 커밋하지 않습니다. 예시 템플릿만 참고하세요.

```bash
cp backend/src/main/resources/db/data.example.sql backend/src/main/resources/db/data.local.sql
```

`data.local.sql`에 로컬 개발용 데이터만 작성한 뒤 local profile로 실행합니다.

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

커밋 가능한 파일은 `data.example.sql`, `seed.example.json` 같은 템플릿뿐입니다. `data.local.sql`, `seed.local.sql`, `*.local.csv`, `*.local.json`, `backend/src/main/resources/seed/local/**`는 `.gitignore`로 제외됩니다.

### Frontend Local

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at: `http://localhost:3000`

## Documentation

Asset Map은 사용자-AI 협업 과정을 투명하게 관리하기 위해 상세 문서를 프로젝트 내부에 포함합니다.

- [Docs Home](./docs/README.md)
- [Project Context](./docs/project/project-context.md)
- [Requirements](./docs/project/requirements.md)
- [Agent Rules](./AGENTS.md)

## What I Focused On

- **데이터 무결성**: 자산과 배당 데이터의 연계성을 고려한 RDB 모델링
- **시각화 최적화**: Recharts를 활용하여 복잡한 자산 데이터를 직관적인 차트로 변환
- **확장성**: 계좌, 종목, 보유 현황을 분리하여 향후 해외 주식, 가상 자산 등으로 확장 가능한 구조 설계
- **협업 가시성**: `docs/` 디렉터리를 통해 AI와의 작업 이력 및 의사결정 과정을 문서화
