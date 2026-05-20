# Asset Map Frontend Architecture

Next.js 16과 React 19를 활용한 Asset Map 프론트엔드는 직관적인 자산 시각화와 고성능 데이터 핸들링을 목표로 합니다.

## 기본 정보
- **경로**: `asset-map/frontend`
- **프레임워크**: Next.js 16.2 (App Router)
- **공통 규칙**: `docs/common/rules/` (코딩 규칙, UI/레이아웃 참조)

## 기술 스택 (Tech Stack)

| Category | Technology | Description |
| --- | --- | --- |
| **Framework** | Next.js 16.2 | App Router 기반의 서버 사이드 렌더링 및 라우팅 |
| **Library** | React 19 | 최신 React 기능 (Action, Suspense 등) 활용 |
| **Styling** | Tailwind CSS 4 | 유틸리티 퍼스트 디자인 시스템 |
| **Data Fetching** | TanStack Query 5 | 서버 상태 관리 및 캐싱 |
| **Visualization** | Recharts 3.8 | 선언적 차트 라이브러리 (Pie, Bar, Line) |
| **Form/Validation** | React Hook Form, Zod | 폼 관리 및 스키마 기반 검증 |

## UI 구조 및 레이아웃

### 라우트 구성
| Route | 상태 | 설명 |
| --- | --- | --- |
| `/` | Implemented | 기본 홈 화면 (앱 허브) |
| `/assets` | Implemented | 전체 자산 대시보드 (요약, 비중 차트, 보유 목록) |
| `/accounts` | Implemented | 계좌 목록 및 상세 관리 |
| `/dividends` | Implemented | 배당 달력 및 월별/연도별 배당 통계 |
| `/securities` | Implemented | 종목 마스터 조회 및 상세 정보 |
| `/settings` | Partial | 자산 분류 체계 및 사용자 설정 |

### 컴포넌트 설계 원칙
- **Shared Components**: `Button`, `Input`, `Modal`, `Card`, `Badge` 등 공통 UI 요소.
- **Domain Components**:
  - `AssetChart`, `DividendCalendar`: 도메인 시각화 요소.
  - `SecurityCombobox`: 검색 및 자동완성 기능을 포함한 종목 선택기.
  - `TradeForm`, `AccountForm`: 도메인 데이터 입력 폼.

## 상태 관리 및 데이터 흐름

### 서버 상태 (Server State)
- **TanStack Query**를 사용하여 백엔드 API와의 통신을 관리합니다.
- `useQuery`를 통한 데이터 fetch 및 캐싱, `useMutation`을 통한 자산 정보 수정/삭제.

### 클라이언트 상태 (Local State)
- 단순 UI 상태(모달 열림/닫힘, 탭 전환)는 `useState`를 사용합니다.
- 복잡한 폼 상태는 **React Hook Form**과 **Zod**를 연동하여 관리합니다.

## 시각화 전략 (Data Visualization)
- **Pie Chart**: 자산군별(주식, 채권, 현금) 및 국가별 비중 시각화.
- **Bar Chart**: 계좌별 자산 분포 및 월별 예상 배당금 표시.
- **Line Chart**: 시간 흐름에 따른 총 자산 가치 변화(타임라인) 추적.
- 모든 차트는 반응형으로 설계되어 모바일 뷰에서도 최적화된 경험을 제공합니다.

## API 연동 방식

### API Client
- `src/lib/api-client.ts`에서 전역 `fetch` 설정을 관리합니다.
- 백엔드의 `ApiResponse<T>` 포맷을 인터페이스로 정의하여 타입 안정성을 확보합니다.

### 환경 변수 설정
- Local 개발: `.env.local`에 `NEXT_PUBLIC_API_BASE_URL=http://localhost:8080`
- 빌드 타임에 API 주소를 주입하여 환경별 유연한 대응이 가능하도록 구성합니다.

## 실행 및 검증 (Run & Verify)

### 로컬 실행
```bash
cd frontend
npm install
npm run dev
```
- Frontend: `http://localhost:3000`

### 린트 및 빌드
```bash
npm run lint    # 정적 분석 및 잠재적 오류 체크
npm run build   # 프로덕션 빌드 최적화
```
