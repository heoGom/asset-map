# Asset Map Project Context

작성일: 2026-05-16

## 문서 목적

이 문서는 Asset Map 프로젝트의 최상위 프로젝트 컨텍스트입니다. 프로젝트의 전체적인 목적과 구성, 현재 구현 범위를 파악하기 위해 사용됩니다.

## 작업 및 문서 확인 순서

Asset Map 프로젝트 작업을 수행할 때는 다음 순서로 문서를 확인하고 작업합니다.

1. **작업 규칙**: 저장소 루트의 [AGENTS.md](../../AGENTS.md) 확인
2. **문서 개요**: [docs/README.md](../README.md)의 문서 구성과 읽는 순서 확인
3. **전체 컨텍스트**: [project-context.md](./project-context.md) (본 문서)의 구현 범위와 구조 파악
4. **영역별 상세**: [backend.md](../architecture/backend.md), [frontend.md](../architecture/frontend.md)의 기술 스택과 실행 정보 확인
5. **공통 규칙**: `docs/common/rules/*`, `docs/common/operations/*` 확인
6. **협업 이력**: [prompts.md](../history/prompts.md), [decisions.md](../history/decisions.md), [work-log.md](../history/work-log.md) 확인

## 프로젝트 개요

Asset Map은 자산 정보를 지도화하고 관리하기 위한 풀스택 프로젝트입니다. 현재는 Spring Boot backend에서 자산/배당 관리의 핵심 도메인과 대시보드 API까지 구현되어 있고, frontend는 기본 뼈대 상태입니다.

## 현재 구현 범위

- Backend 공통 기반: `ApiResponse`, `ErrorCode`, `BusinessException`, `GlobalExceptionHandler`, `BaseEntity`, JPA Auditing, Health API
- Backend 핵심 도메인: `Account`, `SecurityItem`, `SecurityClassification`, `Holding`, `HoldingSnapshot`, `DividendEvent`, `DividendPayment`
- Backend 대시보드 API: 자산 요약/비중, 스냅샷 타임라인, 배당 요약/월별/연도별/종목별/성장률 API
- Frontend: Next.js, TypeScript, Tailwind CSS 기반 기본 홈 화면
- 공통: 루트 README 및 프로젝트 내부 문서 구조 정리, GitHub 원격 저장소 연결 완료
- 작업 규칙: 저장소 루트 `AGENTS.md`와 내부 `docs/common/` 규칙을 기준으로 독립 운영

## 프로젝트 구성

| 구분 | 실제 확인 경로 | 상세 문서 |
| --- | --- | --- |
| Backend | `backend` | [backend.md](../architecture/backend.md) |
| Frontend | `frontend` | [frontend.md](../architecture/frontend.md) |
| Common Rules | `docs/common/rules` | [common/rules](../common/rules) |
| Operations | `docs/common/operations` | [common/operations](../common/operations) |
| Requirements | `docs/project/requirements.md` | [requirements.md](./requirements.md) |
| Prompts | `docs/history/prompts.md` | [prompts.md](../history/prompts.md) |
| Decisions | `docs/history/decisions.md` | [decisions.md](../history/decisions.md) |
| Work Log | `docs/history/work-log.md` | [work-log.md](../history/work-log.md) |

## 문서 유지보수 규칙

- Asset Map 프로젝트의 기획과 요구사항은 `docs/project/`에서 관리합니다.
- 프롬프트, 의사결정, 작업 일지는 `docs/history/`에서 관리합니다.
- Asset Map 작업의 단일 진입점은 저장소 루트 `AGENTS.md`입니다.
- 공통 개발 규칙은 저장소 내부 `docs/common/`을 우선합니다.
- 기능 구현 또는 실행 방식이 변경되면 관련 문서를 함께 업데이트합니다.
