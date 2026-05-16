# Asset Map Documentation Hub

이 디렉터리는 **Asset Map** 프로젝트의 설계, 구현, 그리고 사용자-AI 협업의 모든 과정을 기록하는 통합 문서 공간입니다. 프로젝트의 독립성을 유지하며, 어떤 환경에서도 AI와 협업할 수 있는 최신의 상태(SSoT, Single Source of Truth)를 제공합니다.

---

## 📂 문서 구조

프로젝트의 각 영역은 아래와 같이 구조화되어 관리됩니다.

### 🎯 [Project Context & Requirements](./project)
- **[Project Context](./project/project-context.md)**: 프로젝트 전체 범위, 현재 구현 상태 및 구성 개요.
- **[Requirements](./project/requirements.md)**: 초기 기획부터 단계별 기능 요구사항 명세.

### 🏗️ [Architecture & Implementation](./architecture)
- **[Backend Architecture](./architecture/backend.md)**: Spring Boot 기반의 도메인 설계, 패키지 구조 및 API 명세.
- **[Frontend Architecture](./architecture/frontend.md)**: Next.js 기반의 컴포넌트 구조 및 상태 관리 전략.

### 📜 [Collaboration History](./history)
- **[Prompts & Decisions](./history/decisions.md)**: 주요 아키텍처 결정 및 AI에게 전달된 핵심 프롬프트 기록.
- **[Work Log](./history/work-log.md)**: 일자별 작업 내역 및 업데이트 히스토리.

### 🛠️ [Common Rules & Operations](./common)
- **[Development Rules](./common/rules)**: 코딩 컨벤션, API 설계 원칙, 테스트 전략 등 공통 규칙.
- **[Standard Operations](./common/operations)**: 파일 명세, 빌드 절차 등 반복되는 작업 가이드라인.

---

## 🚀 빠른 시작 가이드 (Reading Order)

AI와 협업을 시작하거나 새로운 환경에서 프로젝트를 파악할 때 다음 순서를 권장합니다.

1.  **[AGENTS.md](../AGENTS.md)**: 작업의 시작점이며, AI가 준수해야 할 최상위 규칙을 확인합니다.
2.  **[Project Context](./project/project-context.md)**: 현재 프로젝트가 어디까지 진행되었는지 확인합니다.
3.  **[Backend 상세](./architecture/backend.md) / [Frontend 상세](./architecture/frontend.md)**: 기술 스택과 실행 방법을 파악합니다.
4.  **[History](./history)**: 이전 작업의 맥락과 의사결정 과정을 이해합니다.

---

## ⚖️ 문서 관리 원칙

- **현행화**: 기능이 변경되거나 아키텍처가 수정되면 반드시 관련 문서를 동기화합니다.
- **독립성**: 상위 워크스페이스의 문서 없이도 이 저장소만으로 모든 작업이 가능하도록 내부 문서를 우선합니다.
- **가독성**: 표, 코드 블록, 다이어그램(Mermaid 등)을 적극 활용하여 직관적인 정보를 제공합니다.
