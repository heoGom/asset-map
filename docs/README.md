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
- **[Codex Workflow](./common/operations/codex-workflow.md)**: Codex/AI 작업 시작, 조사, 구현, 검증, 보안 확인, commit/push 절차.

---

## 🚀 빠른 시작 가이드 (Reading Order)

AI와 협업을 시작하거나 새로운 환경에서 프로젝트를 파악할 때 다음 순서를 권장합니다.

1.  **[AGENTS.md](../AGENTS.md)**: AI가 준수해야 할 최상위 규칙과 작업의 시작점 확인.
2.  **[Project Context](./project/project-context.md)**: 현재 프로젝트 진척도와 핵심 도메인 파악.
3.  **[Architecture](./architecture)**: [Backend](./architecture/backend.md) 및 [Frontend](./architecture/frontend.md) 상세 설계와 실행 방법 확인.
4.  **[Requirements](./project/requirements.md)**: 구현된 기능과 예정된 마일스톤 확인.
5.  **[Codex Workflow](./common/operations/codex-workflow.md)**: 반복 작업 절차와 보안/검증 프로세스 숙지.
6.  **[History](./history)**: 최근 [작업 내역](./history/work-log.md)과 주요 [의사결정](./history/decisions.md) 맥락 이해.

---

## ⚖️ 문서 관리 원칙

- **현행화**: 기능이 변경되거나 아키텍처가 수정되면 관련 문서를 동기화합니다. 주요 결정이나 큰 흐름은 필요할 때 `docs/history/`에 남깁니다.
- **독립성**: 상위 워크스페이스의 문서 없이도 이 저장소만으로 모든 작업이 가능하도록 내부 문서를 우선합니다.
- **가독성**: 표, 코드 블록, 다이어그램(Mermaid 등)을 적극 활용하여 직관적인 정보를 제공합니다.
