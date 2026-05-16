# Asset Map Project Context

작성일: 2026-05-16

## 문서 목적

이 문서는 Asset Map 프로젝트의 최상위 프로젝트 컨텍스트입니다. 현재는 기능 구현 전 단계의 기본 풀스택 프로젝트 구조와 실행 범위를 기록합니다.

## 프로젝트 개요

Asset Map은 자산 정보를 지도화하고 관리하기 위한 풀스택 프로젝트입니다. 첫 단계의 목표는 Spring Boot backend와 Next.js frontend가 각각 정상 실행되는 기본 뼈대를 생성하는 것입니다.

## 현재 구현 범위

- Backend: Spring Boot 3, Java 17, Gradle 기반 기본 애플리케이션
- Frontend: Next.js, TypeScript, Tailwind CSS 기반 기본 홈 화면
- 공통: 루트 README 및 프로젝트 내부 문서 작성
- Git: GitHub 원격 저장소 연결 및 `main` 브랜치 초기 push 완료

## 프로젝트 구성

| 구분 | 실제 확인 경로 | 상세 문서 |
| --- | --- | --- |
| Backend | `backend` | [backend.md](./backend.md) |
| Frontend | `frontend` | [front.md](./front.md) |
| Requirements | `docs/requirements.md` | [requirements.md](./requirements.md) |
| Work Log | `docs/work-log.md` | [work-log.md](./work-log.md) |

## 문서 유지보수 규칙

- Asset Map 프로젝트의 기획, 요구사항, 프롬프트, 의사결정, 작업 일지는 저장소 내부 `docs/`에서 관리합니다.
- 공통 워크스페이스 규칙은 상위 워크스페이스의 `AGENTS.md`와 `docs/common/`을 따릅니다.
- 기능 구현 또는 실행 방식이 변경되면 관련 문서를 함께 업데이트합니다.
