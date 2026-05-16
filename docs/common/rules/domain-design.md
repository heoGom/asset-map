# Asset Map Domain Design Rules

Asset Map 프로젝트의 도메인 모델링과 비즈니스 로직 설계를 위한 공통 규칙입니다. 견고한 도메인 설계를 통해 자산 데이터의 무결성과 확장성을 보장합니다.

---

## 🏗️ 핵심 원칙 (Core Principles)

### 1. 엔티티 기반 구조 (Entity-Based Structure)
- 모든 영속성 엔티티는 `BaseEntity`를 상속받아 생성 및 수정 시각을 자동 관리합니다.
- JPA Auditing(`@CreatedDate`, `@LastModifiedDate`)을 필수로 사용합니다.
- 식별자(ID)는 기본적으로 `Long` 타입의 데이터베이스 자동 증가(Identity) 방식을 권장합니다.

### 2. 패키지 구성 (Feature-Based Packaging)
- 도메인별로 독립적인 패키지를 구성하며, 각 패키지는 아래 요소를 포함합니다.
  ```text
  com.assetmap.backend.{domain}
  ├── {Domain}.java           # JPA 엔티티
  ├── {Domain}Controller.java # API 컨트롤러
  ├── {Domain}Service.java    # 비즈니스 로직
  ├── {Domain}Repository.java # 데이터 접근 계층
  ├── {Domain}Request.java    # 입력 DTO (Create, Update)
  └── {Domain}Response.java   # 출력 DTO
  ```

### 3. 데이터 계산 및 무결성
- **BigDecimal 사용**: 모든 자산 가치, 수익률, 환율 계산에는 부동 소수점 오차 방지를 위해 `double`/`float` 대신 `BigDecimal`을 사용합니다.
- **계산 로직 분리**: 복잡한 자산 합산 및 통계 계산은 `MoneyCalculator`와 같은 전용 유틸리티 또는 서비스 계층으로 캡슐화합니다.

---

## 🛠️ 상세 설계 가이드

### 엔티티 상태 관리
- 현재 단계에서는 물리 삭제(Hard Delete)를 기본으로 하되, 향후 운영 이력 관리가 필요한 도메인(예: 자산 이력)에 대해서는 상태 필드(`ACTIVE`, `DELETED`) 도입을 고려합니다.
- 중요 비즈니스 데이터(보유 현황 등)의 변경은 `HoldingSnapshot`과 같은 별도 엔티티를 통해 스냅샷 형태로 보관합니다.

### DTO 및 데이터 변환
- **Entity <-> DTO 분리**: 컨트롤러는 엔티티를 직접 반환하거나 입력받지 않습니다. 반드시 Request/Response DTO를 통해 데이터 계약을 맺습니다.
- **명확한 네이밍**: `CreateRequest`, `UpdateRequest`, `DetailResponse`, `SummaryResponse` 등 용도에 맞는 명확한 네이밍을 사용합니다.

### 비즈니스 예외 처리
- 도메인 제약 조건 위반이나 비즈니스 로직 실패 시 `BusinessException`을 발생시킵니다.
- `ErrorCode` 이넘(Enum)을 통해 에러 메시지와 상태 코드를 중앙 집중식으로 관리합니다.

---

## ⚖️ 도메인별 특화 규칙

| 도메인 | 규칙 |
| --- | --- |
| **Account** | 증권사 명칭과 계좌 유형은 정해진 Enum 값을 사용하여 데이터 일관성을 유지함. |
| **SecurityItem** | 티커(Ticker)는 대문자로 관리하며, 국가별 시장 구분을 포함해야 함. |
| **Holding** | 수량이나 평단가 변경 시 연관된 자산 통계가 즉시 반영되거나 스케줄링을 통해 갱신됨. |
| **Dividend** | 배당락일(Ex-Date)과 지급일(Payment Date)의 시차를 고려하여 상태를 관리함. |
