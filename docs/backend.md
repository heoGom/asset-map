# Asset Map Backend

## 기술 스택

- Spring Boot 3.5.6
- Java 17
- Gradle
- Spring Web
- Spring Data JPA
- Validation
- H2 Database
- Lombok

## 현재 상태

기본 Spring Boot 애플리케이션만 생성되어 있습니다. Spring Security, JWT, 도메인 모델, API 구현은 아직 포함하지 않습니다.

## 실행 방법

```bash
cd backend
./gradlew bootRun
```

기본 실행 주소는 `http://localhost:8080`입니다.

## 검증 방법

```bash
cd backend
./gradlew test
./gradlew build
```
