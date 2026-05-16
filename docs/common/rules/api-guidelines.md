# Asset Map API Guidelines

Asset Map 프로젝트의 백엔드 API 설계 및 응답 규격에 관한 지침입니다. 일관된 API 디자인을 통해 프론트엔드와의 연동 효율성을 높입니다.

---

## 📡 응답 규격 (Response Format)

모든 API 응답은 `ApiResponse<T>` 래퍼 클래스를 사용하여 통일된 구조를 유지합니다.

### 1. 성공 응답 (Success)
```json
{
  "status": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... }
}
```
- `data`: 실제 비즈니스 데이터를 포함합니다. (단건, 목록, 혹은 단순 ID 등)

### 2. 에러 응답 (Error)
```json
{
  "status": "ERROR",
  "message": "잘못된 요청입니다.",
  "data": {
    "errorCode": "INVALID_INPUT_VALUE"
  }
}
```
- 에러 발생 시 `data` 필드에 명확한 에러 코드를 포함하여 클라이언트가 대응할 수 있도록 합니다.

---

## 🛠️ 설계 원칙 (Design Principles)

### 1. RESTful Path
- 자원을 나타내는 명사를 경로에 사용하며, 행위는 HTTP Method로 구분합니다.
  - `GET /api/accounts`: 계좌 목록 조회
  - `POST /api/accounts`: 계좌 등록
  - `PUT /api/accounts/{id}`: 계좌 정보 수정
  - `DELETE /api/accounts/{id}`: 계좌 삭제

### 2. 일관된 페이지네이션 및 정렬
- 목록 조회 API는 기본적으로 페이지네이션을 지원하며, 파라미터 네이밍을 통일합니다.
  - `page`: 페이지 번호 (0부터 시작)
  - `size`: 한 페이지당 항목 수
  - `sort`: 정렬 기준 필드
  - `direction`: 정렬 방향 (`ASC`, `DESC`)

### 3. 데이터 검증 (Validation)
- 클라이언트로부터 전달받는 모든 입력값은 `@Valid` 및 JSR-303 검증 어노테이션을 사용하여 유효성을 검사합니다.
- 검증 실패 시 `400 Bad Request`와 함께 구체적인 에러 정보를 반환합니다.

---

## 🔒 인증 및 보안 (Auth & Security)

### 1. 인증 방식 (Planned)
- 향후 단계에서는 JWT(JSON Web Token) 기반 인증을 도입할 예정입니다.
- 보안이 필요한 API는 `Authorization: Bearer {token}` 헤더를 요구하게 됩니다.

### 2. 상태 코드 활용
- `200 OK`: 요청 성공
- `201 Created`: 자원 생성 성공
- `400 Bad Request`: 잘못된 파라미터 또는 검증 실패
- `401 Unauthorized`: 인증되지 않은 사용자
- `403 Forbidden`: 권한이 없는 자원에 대한 접근
- `404 Not Found`: 존재하지 않는 자원 요청

---

## 📝 문서화 원칙
- 새로운 API를 추가하거나 변경할 경우, 반드시 `docs/architecture/backend.md`의 API 명세를 현행화합니다.
- 복잡한 로직이 포함된 시나리오는 `docs/project/api-test-scenarios.md`에 테스트 케이스를 추가합니다.
