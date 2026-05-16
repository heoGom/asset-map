# Decisions

## 2026-05-16: 기본 프로젝트 생성 범위 제한

- 결정: backend와 frontend는 실행 가능한 기본 뼈대까지만 생성한다.
- 이유: 현재 목표는 기능 구현이 아니라 프로젝트 초기 구조를 만드는 것이다.
- 영향: Spring Security, JWT, 도메인 모델, API, 복잡한 화면 구현은 포함하지 않는다.

## 2026-05-16: Git 원격 저장소 연결

- 결정: `asset-map` 루트를 독립 Git 저장소로 초기화하고 `origin`을 `git@github.com-private:heoGom/asset-map.git`로 설정한다.
- 이유: 이후 사용자와 AI가 함께 작업한 변경사항을 Git 기록으로 남기기 위해서다.
- 영향: 초기 커밋은 `chore: initialize asset-map project`로 기록하고 `main` 브랜치를 원격에 push했다.

## 2026-05-16: 프로젝트 내부 docs 관리

- 결정: Asset Map 관련 기획, 요구사항, 프롬프트, 의사결정, 작업 일지는 저장소 내부 `docs/`에서 관리한다.
- 이유: 프로젝트가 만들어진 과정과 협업 맥락을 저장소 자체에 남기기 위해서다.
- 영향: 상위 워크스페이스 문서는 공통 작업 규칙 확인 용도로 유지하고, Asset Map 고유 이력은 내부 문서를 우선 갱신한다.

## 2026-05-16: Asset Map 저장소를 독립 작업 루트로 운영

- 결정: 저장소 루트에 `AGENTS.md`를 추가하고, 공통 규칙과 운영 절차를 `docs/common/`에 포함한다.
- 이유: 상위 `dev` 워크스페이스에 의존하지 않고도 Asset Map 저장소만으로 AI 작업 규칙, 구현 규칙, 협업 이력을 확인할 수 있어야 한다.
- 영향: Asset Map 작업의 단일 진입점은 `asset-map/AGENTS.md`가 되며, 상위 워크스페이스 문서는 Asset Map의 위치와 독립 운영 사실만 안내한다.

## 2026-05-16: 문서 디렉터리 목적별 구조화

- 결정: `docs/` 루트에 흩어져 있던 문서를 `project`, `architecture`, `history`, `common`으로 구조화한다.
- 이유: 프로젝트 요구사항, 구현 정보, 협업 이력, 공통 규칙의 성격이 달라서 같은 디렉터리에 두면 작업자가 필요한 문서를 찾기 어렵다.
- 영향: `AGENTS.md`와 문서 링크는 새 경로를 기준으로 갱신한다.

## 2026-05-16: Homepage 수준의 문서 순서 체계 반영

- 결정: Asset Map 문서에도 `homepage`처럼 작업 및 문서 확인 순서, 문서 읽는 순서를 명시한다.
- 이유: 작업 흐름이 명확히 드러나야 사용자와 AI가 현재 상태를 같은 기준으로 이해할 수 있다.
- 영향: `docs/project/project-context.md`, `docs/README.md`, `docs/architecture/backend.md`를 현재 구현 기준과 함께 갱신한다.
