# Prompts

이 문서는 Asset Map 프로젝트 진행 중 주요 사용자 프롬프트를 기록합니다. 긴 프롬프트는 의미를 보존하는 범위에서 요약합니다.

## 2026-05-16: 프로젝트 뼈대 생성

사용자는 `asset-map` 프로젝트 생성을 요청했습니다.

핵심 내용:

- 루트 구조: `asset-map/backend`, `asset-map/frontend`
- backend: Spring Boot 3, Java 17, Gradle
- backend 의존성: Spring Web, Spring Data JPA, Validation, H2 Database, Lombok
- Spring Security, JWT, 복잡한 도메인 구현 제외
- frontend: Next.js, TypeScript, Tailwind CSS
- frontend 추가 라이브러리: Recharts, TanStack React Query, React Hook Form, Zod
- 복잡한 화면 구현 제외, 기본 홈 화면만 작성
- 루트 README 작성
- backend 실행, frontend 실행, 빌드 오류 없음, README 작성 여부 확인

## 2026-05-16: GitHub 원격 저장소 연결

사용자는 현재까지 작업된 내용을 Git 기록으로 남기기 위해 GitHub 원격 저장소 연결과 첫 커밋, 첫 push를 요청했습니다.

핵심 내용:

- 원격 저장소: `git@github.com-private:heoGom/asset-map.git`
- Git 저장소가 아니면 `git init`
- 루트 `.gitignore` 추가
- `origin` 원격 설정
- 커밋 메시지: `chore: initialize asset-map project`
- `main` 브랜치 사용
- `git push -u origin main`
- SSH 인증 문제가 있으면 에러 메시지를 그대로 보여주고 중단

## 2026-05-16: 프로젝트 내부 문서화

사용자는 Asset Map 프로젝트와 사용자-AI 협업 과정을 저장소 안에 남기기 위해 프로젝트 내부 `docs/`에서 관련 문서를 관리하도록 요청했습니다.

핵심 내용:

- 기존에는 상위 워크스페이스 `dev/AGENTS.md`가 단일 진입점이었다.
- 기존 프로젝트 문서는 `dev/docs/projects/asset-map/` 아래에서 관리되었다.
- 앞으로 Asset Map 관련 기획, 요구사항, 프롬프트, 의사결정, 작업 일지는 프로젝트 내부 `docs/`에 함께 관리한다.
