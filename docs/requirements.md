# Requirements

## 초기 프로젝트 생성 요구사항

- 프로젝트명은 `asset-map`으로 한다.
- 루트 구조는 `backend`, `frontend`로 구성한다.
- backend는 Spring Boot 3, Java 17, Gradle 기반으로 생성한다.
- backend 필수 의존성은 Spring Web, Spring Data JPA, Validation, H2 Database, Lombok이다.
- 현재 단계에서는 Spring Security, JWT, 복잡한 도메인 구현을 하지 않는다.
- frontend는 Next.js, TypeScript, Tailwind CSS 기반으로 생성한다.
- frontend 추가 라이브러리는 Recharts, TanStack React Query, React Hook Form, Zod이다.
- 현재 단계에서는 복잡한 화면 구현 없이 기본 홈 화면만 만든다.
- 루트 `README.md`에는 프로젝트 개요, 기술 스택, 디렉터리 구조, backend/frontend 실행 방법을 적는다.
- 작업 후 backend 실행, frontend 실행, 빌드 오류 없음, README 작성 여부를 확인한다.

## Git 관리 요구사항

- `asset-map` 프로젝트 루트를 Git 저장소로 초기화한다.
- GitHub 원격 저장소는 `git@github.com-private:heoGom/asset-map.git`을 사용한다.
- `main` 브랜치를 사용한다.
- 기본 프로젝트 생성 결과를 `chore: initialize asset-map project` 커밋으로 기록한다.
- `git push -u origin main`으로 원격 저장소에 push한다.
- 앞으로 Codex와 함께 작업하는 모든 변경사항을 Git 기록으로 남긴다.

## 문서 관리 요구사항

- 원래 작업은 상위 워크스페이스 `dev/AGENTS.md` 규칙을 따른다.
- Asset Map과 관련된 기획, 요구사항, 프롬프트, 의사결정, 작업 일지는 프로젝트 내부 `docs/`에 함께 관리한다.
- 사용자와 AI가 함께 만든 과정을 저장소 안에 남기는 것을 목표로 한다.
