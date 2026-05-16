# Asset Map

## 프로젝트 개요

Asset Map은 자산 정보를 지도화하고 관리하기 위한 풀스택 프로젝트입니다. 현재 단계에서는 기능 구현 없이 backend와 frontend 기본 실행이 가능한 프로젝트 뼈대만 구성합니다.

## 기술 스택

- Backend: Spring Boot 3, Java 17, Gradle, Spring Web, Spring Data JPA, Validation, H2 Database, Lombok
- Frontend: Next.js, TypeScript, Tailwind CSS, Recharts, TanStack React Query, React Hook Form, Zod

## 디렉터리 구조

```text
asset-map/
├── README.md
├── backend/
├── docs/
└── frontend/
```

## 프로젝트 문서

Asset Map의 기획, 요구사항, 프롬프트, 의사결정, 작업 일지는 [docs](./docs/README.md)에서 관리합니다.

## backend 실행 방법

```bash
cd backend
./gradlew bootRun
```

기본 실행 주소는 `http://localhost:8080`입니다.

## frontend 실행 방법

```bash
cd frontend
npm install
npm run dev
```

기본 실행 주소는 `http://localhost:3000`입니다.
