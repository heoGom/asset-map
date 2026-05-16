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
