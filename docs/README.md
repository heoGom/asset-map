# Asset Map Docs

이 디렉터리는 Asset Map 프로젝트와 사용자-AI 협업 과정을 저장소 안에 남기기 위한 문서 공간입니다.

## 문서 구성

- [project](./project): 프로젝트 컨텍스트와 요구사항
- [architecture](./architecture): backend/frontend 구현 구조와 실행 정보
- [history](./history): 프롬프트, 의사결정, 작업 일지
- [common/rules](./common/rules): 프로젝트 공통 개발 규칙
- [common/operations](./common/operations): 반복 작업 절차

## 문서 읽는 순서

1. [AGENTS.md](../AGENTS.md): 작업 규칙과 진입점 확인
2. [project/project-context.md](./project/project-context.md): 프로젝트 전체 범위와 현재 상태 확인
3. [architecture/backend.md](./architecture/backend.md): backend 구현 범위와 실행/검증 방법 확인
4. [architecture/frontend.md](./architecture/frontend.md): frontend 현재 상태 확인
5. [common/rules](./common/rules), [common/operations](./common/operations): 공통 규칙 확인
6. [history](./history): 사용자 요청, 의사결정, 작업 흐름 확인

## 관리 원칙

- Asset Map에 관한 기획과 요구사항은 `docs/project/`에 기록합니다.
- 프롬프트, 의사결정, 작업 일지는 `docs/history/`에 기록합니다.
- backend/frontend 구현 정보는 `docs/architecture/`에 기록합니다.
- 기능 구현 또는 실행 방식이 변경되면 관련 문서를 함께 업데이트합니다.
- Asset Map 작업의 진입점은 저장소 루트의 `AGENTS.md`입니다.
- 공통 규칙은 저장소 내부 `docs/common/`을 우선합니다.
