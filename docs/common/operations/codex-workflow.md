# Codex 작업 흐름

이 문서는 Asset Map 저장소에서 Codex 또는 AI 작업자가 반복적으로 따라야 하는 표준 작업 절차입니다. 최상위 규칙은 저장소 루트의 `AGENTS.md`이며, 이 문서는 세부 실행 절차를 보완합니다.

## 1. 작업 시작

1. 현재 작업 루트가 `asset-map`인지 확인합니다.
2. `git status --short --branch`로 기존 변경사항을 확인합니다.
3. 다음 문서를 순서대로 읽습니다.
   - `AGENTS.md`
   - `docs/project/project-context.md`
   - `docs/README.md`
   - 작업 영역에 맞는 `docs/architecture/*`, `docs/common/rules/*`, `docs/common/operations/*`, `docs/history/*`
4. 요구사항이 모호하거나 작업 대상 프로젝트가 불명확하면 코드나 문서를 수정하기 전에 사용자에게 확인합니다.

## 2. 사전 조사

1. `rg`로 관련 파일, 클래스, API, 문서 항목을 검색합니다.
2. 기존 controller, service, repository, entity, DTO, test 구조를 먼저 확인합니다.
3. 기존 패턴을 우선 따르고, 새 추상화는 중복 제거 또는 정책 공통화가 실제로 필요할 때만 추가합니다.
4. 사용자가 “검토만”, “수정하지 마라”라고 지시한 경우 파일 읽기와 diff 확인만 수행합니다.

## 3. 수정 전 공유

요청 범위가 작지 않거나 사용자가 요구한 경우, 수정 전에 다음을 먼저 보고합니다.

- 수정 대상 파일 목록
- 각 파일을 수정하는 이유
- 짧은 구현 계획
- 예상 검증 방법

## 4. 구현 원칙

- 관련 없는 리팩토링을 하지 않습니다.
- 기존 사용자 변경사항을 되돌리지 않습니다.
- API 응답에는 JPA entity를 직접 반환하지 않고 DTO를 사용합니다.
- backend 로직은 controller를 얇게 유지하고 service 계층에 둡니다.
- 금액 계산은 `BigDecimal`을 기준으로 합니다.
- frontend 변경은 반응형, 접근성, 다크/라이트 모드 가독성을 함께 고려합니다.
- test profile에서 외부 API 호출이 발생하지 않도록 유지합니다.
- 민감정보 파일과 실제 API key 값은 절대 수정하거나 커밋하지 않습니다.

## 5. 테스트와 검증

Backend 변경 시 기본 검증은 다음 순서로 수행합니다.

```bash
cd backend
./gradlew build
```

필요하면 더 좁은 범위의 테스트를 먼저 실행할 수 있지만, 완료 전에는 관련 검증 결과를 보고합니다.

Frontend 변경 시 기본 검증은 다음 순서로 수행합니다.

```bash
cd frontend
npm run build
```

UI 변경이 있으면 가능한 경우 로컬 화면에서 주요 viewport와 다크/라이트 모드를 확인합니다.

## 6. 문서 동기화

기능, 정책, 아키텍처, 작업 절차가 바뀌면 관련 문서를 함께 갱신합니다.

- 구조/정책 변경: `docs/architecture/backend.md` 또는 `docs/architecture/frontend.md`
- 작업 이력: `docs/history/work-log.md`
- 주요 결정: `docs/history/decisions.md`
- 공통 규칙: `docs/common/rules/*`
- 반복 절차: `docs/common/operations/*`
- 문서 허브 링크: `docs/README.md`

## 7. 보안 확인

커밋 전 다음을 확인합니다.

```bash
git status --short --branch
git diff --name-only
git diff --stat
git diff --check
```

stage 후에는 staged diff도 확인합니다.

```bash
git diff --cached --name-only
git diff --cached --stat
git diff --cached --check
```

다음 항목은 diff와 staged 파일에 포함되면 안 됩니다.

- 실제 API key 값
- `KRX_API_KEY`
- `PUBLIC_DATA_STOCK_DIVIDEND_SERVICE_KEY`
- `.local-secrets.properties`
- `backend/.local-secrets.properties`
- `backups/*.sql`
- 개인 로컬 경로
- 실제 투자 full seed
- build 결과물, `node_modules`

환경변수명이나 Git 제외 정책을 설명하는 문서상 언급은 가능하지만, 실제 secret 값은 남기지 않습니다.

## 8. Commit과 Push

commit/push는 사용자가 명시적으로 요청한 경우에만 수행합니다.

1. 변경 파일과 diff를 확인합니다.
2. 민감정보 포함 여부를 확인합니다.
3. 필요한 파일만 stage합니다.
4. 사용자가 지정한 commit message가 있으면 그대로 사용합니다.
5. push 후 commit hash와 push 결과를 보고합니다.

예:

```bash
git add <files>
git commit -m "docs: add Codex workflow guide"
git push origin main
```

## 9. 완료 보고

완료 보고에는 다음을 포함합니다.

- 변경 파일
- 구현 또는 문서화 내용
- 실행한 검증 명령과 결과
- 민감정보 포함 여부 확인 결과
- commit/push 여부
- 남은 리스크
