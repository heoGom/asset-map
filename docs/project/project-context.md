# Asset Map Project Context

작성일: 2026-05-16

## 문서 목적

이 문서는 **Asset Map** 프로젝트의 최상위 프로젝트 컨텍스트입니다. 프로젝트의 전체적인 목적과 구성, 현재 구현 범위를 파악하기 위해 사용됩니다.

## 작업 및 문서 확인 순서

Asset Map 프로젝트 작업을 수행할 때는 다음 순서로 문서를 확인하고 작업해야 합니다.

1. **작업 규칙**: 저장소 루트의 [AGENTS.md](../../AGENTS.md) 확인 (AI 작업의 단일 진입점)
2. **문서 개요**: [docs/README.md](../README.md)의 문서 구성과 읽는 순서 확인
3. **전체 컨텍스트**: [project-context.md](./project-context.md) (본 문서)의 구현 범위와 마일스톤 확인
4. **영역별 상세**: [backend.md](../architecture/backend.md), [frontend.md](../architecture/frontend.md)의 기술 스택, API 명세, 실행 정보 확인
5. **공통 규칙**: `docs/common/rules/*`, `docs/common/operations/*` 확인
6. **수동 검증**: [api-test-scenarios.md](./api-test-scenarios.md)로 핵심 API 확인
7. **협업 이력**: [decisions.md](../history/decisions.md), [work-log.md](../history/work-log.md) 등을 통해 최근 변경 사항 파악

## 프로젝트 개요

Asset Map은 개인의 자산 정보를 지도화(Mapping)하고 시각적으로 관리하기 위한 풀스택 프로젝트입니다. 증권 계좌, 보유 종목, 배당 흐름을 통합적으로 관리하여 자산 현황을 한눈에 파악하고 미래 수익을 예측하는 데 초점을 맞춥니다.

### 현재 구현 범위

- **Backend 공통 기반**: `ApiResponse`, `ErrorCode`, 전역 예외 처리, JPA Auditing, Health API
- **핵심 도메인**:
  - 계좌(`Account`): 증권사별 계좌 관리 및 유형 분류
  - 종목(`SecurityItem`): 티커, 섹터, 국가, 자산군 등 종목 마스터 정보
  - 분류(`SecurityClassification`): 종목별 맞춤형 분류 체계
  - 보유(`Holding`): 계좌별 종목 보유 수량 및 평단가 관리
- **이력 및 분석**:
  - 스냅샷(`HoldingSnapshot`): 주기적인 자산 상태 저장 및 타임라인 분석
  - 배당(`Dividend`): 배당 이벤트(예상) 및 실제 지급 내역 관리
- **대시보드 API**: 자산 요약, 비중 통계, 월별/연도별 배당 성장률 분석
- **Frontend**: Next.js 16 (App Router) 기반 기본 홈 화면 및 스타일링 설정 완료

## 프로젝트 구성

| 구분 | 실제 확인 경로 | 상세 문서 |
| --- | --- | --- |
| Backend | `backend` | [backend.md](../architecture/backend.md) |
| Frontend | `frontend` | [frontend.md](../architecture/frontend.md) |
| Common Rules | `docs/common/rules` | [common/rules](../common/rules) |
| Requirements | `docs/project/requirements.md` | [requirements.md](./requirements.md) |
| Test Scenarios | `docs/project/api-test-scenarios.md` | [api-test-scenarios.md](./api-test-scenarios.md) |
| Decisions | `docs/history/decisions.md` | [decisions.md](../history/decisions.md) |
| Work Log | `docs/history/work-log.md` | [work-log.md](../history/work-log.md) |

## 문서 유지보수 규칙

- **독립 운영**: Asset Map 저장소는 상위 워크스페이스 문서에 의존하지 않고도 작업할 수 있도록 내부 문서를 최신 상태(SSoT)로 유지합니다.
- **현행화**: 기능 구현 또는 아키텍처 변경 시 관련 문서를 즉시 업데이트합니다. 중요한 결정이나 변경은 필요할 때 `docs/history/`에 기록합니다.
- **중복 제거**: 공통 규칙은 `docs/common/`에서 관리하며, 개별 문서에서는 해당 프로젝트 고유의 사실만 기록합니다.
