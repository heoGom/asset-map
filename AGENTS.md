# AI 작업 규칙 (Asset Map)

이 문서는 Asset Map 저장소에서 AI가 작업을 수행할 때 가장 먼저 읽고 준수해야 하는 프로젝트 루트 가이드라인입니다.

## 작업 순서

AI는 모든 작업 시 다음 절차를 따릅니다.

1. **사전 조사**
   - 1순위: `AGENTS.md` (본 문서)
   - 2순위: `docs/project/project-context.md`
   - 3순위: 작업 영역별 문서
     - 공통 개발 규칙: `docs/common/rules/*`
     - 반복 작업 절차: `docs/common/operations/*`
     - backend 상세: `docs/architecture/backend.md`
     - frontend 상세: `docs/architecture/frontend.md`
     - 요구사항 및 작업 이력: `docs/project/requirements.md`, `docs/history/decisions.md`, `docs/history/work-log.md`

2. **작업 범위 확인**
   - 작업 대상은 이 저장소 루트(`asset-map`)를 기준으로 판단합니다.
   - 요구사항이 모호하면 임의로 구현하지 말고 사용자에게 확인합니다.

3. **전략 및 구현**
   - 작고 명확한 단위로 변경합니다.
   - 관련 없는 수정은 배제합니다.
   - 기능 수정에는 필요한 테스트 코드를 함께 포함합니다.

4. **문서 동기화**
   - 기획과 요구사항은 `docs/project/`에 기록합니다.
   - 프롬프트, 의사결정, 작업 일지는 `docs/history/`에 기록합니다.
   - 구현 방식이나 실행 방법이 바뀌면 `docs/project/project-context.md`, `docs/architecture/backend.md`, `docs/architecture/frontend.md`, `README.md` 중 관련 문서를 함께 업데이트합니다.

5. **Git 기록**
   - 사용자와 AI가 함께 작업한 변경사항은 Git 커밋으로 남깁니다.
   - 커밋 전 `git status`로 변경 범위를 확인합니다.
   - 불필요한 빌드 산출물과 의존성 폴더는 커밋하지 않습니다.

## 핵심 원칙

- **프로젝트 독립성**: 이 저장소는 상위 워크스페이스 문서에 의존하지 않고도 작업할 수 있어야 합니다.
- **단일 진입점**: Asset Map 작업의 시작점은 이 파일(`AGENTS.md`)입니다.
- **SSoT**:
  - 공통 개발 규칙: `docs/common/rules/*`
  - 반복 작업 절차: `docs/common/operations/*`
  - 프로젝트 컨텍스트와 요구사항: `docs/project/*`
  - 구현 정보: `docs/architecture/*`
  - 협업 이력: `docs/history/*`
- **중복 방지**: backend/frontend 내부에는 별도 `AGENTS.md`를 두지 않습니다.
- **충돌 해결**: 내부 공통 규칙과 프로젝트 문서가 충돌하면 사용자에게 보고하고, 합의된 내용을 내부 문서에 반영합니다.

## 프로젝트 경로

- Backend: `backend`
- Frontend: `frontend`
- Docs: `docs`
