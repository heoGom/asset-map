# Work Log

## 2026-05-16

### 프로젝트 뼈대 생성

- `asset-map/backend` 생성
- Spring Boot 3.5.6, Java 17, Gradle 설정
- backend 의존성 추가: Spring Web, Spring Data JPA, Validation, H2 Database, Lombok
- `asset-map/frontend` 생성
- Next.js, TypeScript, Tailwind CSS 설정
- frontend 의존성 추가: Recharts, TanStack React Query, React Hook Form, Zod
- 기본 홈 화면을 Asset Map용 단순 화면으로 정리
- 루트 `README.md` 작성
- backend `./gradlew test`, `./gradlew build` 성공 확인
- backend `./gradlew bootRun`으로 8080 실행 확인
- frontend `npm run build` 성공 확인
- frontend `npm run dev`로 3000 실행 확인

### Git 초기화 및 원격 push

- `asset-map` 루트에서 `git init` 실행
- 루트 `.gitignore` 추가
- `origin`을 `git@github.com-private:heoGom/asset-map.git`로 설정
- 첫 커밋 생성: `chore: initialize asset-map project`
- `git push -u origin main` 성공

### 프로젝트 내부 문서화

- 저장소 내부 `docs/` 디렉터리 추가
- 프로젝트 컨텍스트, backend, frontend 문서를 내부로 복제 및 정리
- 요구사항, 프롬프트, 의사결정, 작업 일지 문서 추가

### 독립 작업 루트 문서화

- 저장소 루트 `AGENTS.md` 추가
- 상위 워크스페이스의 공통 규칙 Markdown을 `docs/common/`으로 가져옴
- Asset Map 작업은 내부 `AGENTS.md`와 `docs/common/`을 우선하도록 문서 정리

### 문서 구조화

- `docs/project/` 추가: 프로젝트 컨텍스트와 요구사항 관리
- `docs/architecture/` 추가: backend/frontend 구현 정보 관리
- `docs/history/` 추가: 프롬프트, 의사결정, 작업 일지 관리
- `AGENTS.md`와 문서 내부 링크를 새 구조에 맞게 수정
