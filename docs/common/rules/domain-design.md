# 도메인 설계 규칙

## 목적

이 문서는 프로젝트 전반에서 사용되는 공용 도메인 설계 규칙을 정의합니다.
백엔드 도메인을 생성하거나 수정할 때 이 규칙을 따르십시오.

## 핵심 규칙

- 모든 영속성 엔티티는 `BaseEntity`를 상속받아야 합니다.
- `BaseEntity`는 `createdAt`과 `updatedAt`을 포함해야 합니다.
- JPA 오디팅(Auditing)이 활성화되어야 합니다.
- 도메인 상태는 `EntityStatus.ACTIVE`와 `EntityStatus.DELETED`만 사용해야 합니다.
- `UPDATED` 상태는 사용하지 않습니다.
- 삭제는 `status = DELETED`로 설정하는 소프트 딜리트(soft delete)로 처리해야 합니다.
- 일반적인 쿼리는 삭제된 행을 제외해야 합니다.
- 삭제된 자식 항목이 계층 구조에서 계속 보여야 하는 경우, 숨기는 대신 삭제된 항목이라는 안내 메시지를 표시하십시오.
- 모든 생성, 수정, 삭제 작업은 히스토리 테이블에 기록되어야 합니다.
- 히스토리는 변경 전후 스냅샷을 JSON으로 저장해야 합니다.
- 컨트롤러는 엔티티를 직접 반환해서는 안 됩니다.
- 컨트롤러는 리포지토리를 직접 호출해서는 안 됩니다.
- 요청과 응답은 DTO를 사용해야 합니다.
- 엔티티 상태 변경은 엔티티 메서드를 통해 수행되어야 합니다.
- 비즈니스 에러는 `BusinessException`과 `ErrorCode` 패턴을 사용해야 합니다.
- 수정 및 삭제에 대한 소유권 확인은 서비스 계층에서 처리되어야 합니다.
- 소유권 검증은 `OwnershipValidator`를 사용해야 합니다.
- 리소스 소유권 비교는 null-safe 해야 합니다.
- 좋아요와 같은 반응 데이터는 소프트 딜리트 및 히스토리 규칙에서 제외하여 별도로 처리합니다.
- 조회 이력과 같은 감사 로그는 소프트 딜리트 및 히스토리 규칙에서 제외하여 별도로 처리합니다.

## 공용 패키지 구조

```text
{base-package}.{domain}
├── {Domain}.java
├── {Domain}Controller.java
├── {Domain}Service.java
├── {Domain}Repository.java
├── {Domain}Request.java
├── {Domain}Response.java
├── {Domain}History.java
└── {Domain}HistoryRepository.java
```

공용 타입 위치:

```text
{base-package}.common.entity.BaseEntity
{base-package}.common.entity.EntityStatus
{base-package}.common.history.AuditAction
{base-package}.common.security.OwnableResource
{base-package}.common.security.OwnershipValidator
{base-package}.common.security.SecurityEventLogger
```

## BaseEntity

```java
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

## EntityStatus

```java
public enum EntityStatus {
    ACTIVE,
    DELETED
}
```

## 소프트 딜리트 (Soft Delete)

- 소프트 딜리트로 관리되는 도메인에서 물리 삭제는 허용되지 않습니다.
- 쿼리 메서드는 기본적으로 삭제된 행을 제외해야 합니다.
- 삭제 메서드는 엔티티의 상태만 변경해야 합니다.

## 히스토리 (History)

- 생성, 수정, 삭제 작업은 변경 전후 스냅샷을 기록해야 합니다.
- 히스토리 테이블은 감사가 필요한 비즈니스 엔티티에 대해서만 생성합니다.
- 반응 데이터 및 조회 이력 로그는 동일한 히스토리 패턴을 따르지 않습니다.

## 소유권 (Ownership)

- 수정 및 삭제는 서비스 계층에서 검증되어야 합니다.
- 소유자 인식이 필요한 엔티티는 `OwnableResource`를 구현하십시오.
- 소유권 확인에는 `OwnershipValidator`를 사용하십시오.
