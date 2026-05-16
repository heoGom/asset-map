# 🤖 AI 작업 규칙 (Asset Map)

이 문서는 **Asset Map** 저장소에서 AI가 작업을 수행할 때 반드시 준수해야 하는 최상위 가이드라인입니다. 프로젝트의 일관성을 유지하고 효율적인 협업을 위해 아래 규칙을 철저히 따릅니다.

---

## 🚀 작업 프로세스 (Standard Workflow)

AI는 모든 요청에 대해 다음 5단계 절차를 수행합니다.

### 1. 사전 조사 (Research)
- **1순위**: `AGENTS.md` (본 문서) 확인
- **2순위**: [Project Context](./docs/project/project-context.md) 확인
- **3순위**: 관련 상세 문서 확인
  - 공통 규칙: `docs/common/rules/*`
  - 아키텍처: `docs/architecture/*`
  - 작업 이력: `docs/history/*`

### 2. 범위 및 의도 파악 (Intent Alignment)
- 작업 대상을 이 저장소 루트(`asset-map`)로 한정합니다.
- 요구사항이 모호할 경우 임의로 판단하지 않고 사용자에게 질문합니다.

### 3. 전략 및 구현 (Strategy & Execution)
- 작고 명확한 단위(Atomic Changes)로 코드를 수정합니다.
- 관련 없는 코드의 리팩토링이나 수정은 지양합니다.
- 기능 추가/수정 시 반드시 테스트 코드를 포함하거나 검증 절차를 거칩니다.

### 4. 문서 동기화 (Documentation)
- 기획/요구사항 변경 시 `docs/project/` 문서를 갱신합니다.
- 주요 의사결정 및 작업 내역은 `docs/history/`에 기록합니다.
- 구현 방식 변경 시 `architecture/` 및 `README.md`를 함께 업데이트합니다.

### 5. Git 기록 (Version Control)
- 작업 완료 후 명확한 커밋 메시지와 함께 변경사항을 기록합니다.
- 불필요한 산출물(build/, node_modules/ 등)이 포함되지 않도록 주의합니다.

---

## ⚖️ 핵심 원칙 (Core Principles)

| 원칙 | 설명 |
| --- | --- |
| **독립성 (Isolation)** | 상위 워크스페이스에 의존하지 않고 이 저장소만으로 작업이 가능해야 함 |
| **단일 진입점 (SSoT)** | AI 작업의 모든 시작점은 항상 이 파일(`AGENTS.md`)임 |
| **문서 우선 (Docs First)** | 코드 변경보다 문서화된 계약(API, 도메인 규칙)을 우선시함 |
| **투명성 (Transparency)** | 의사결정 과정과 프롬프트 이력을 `docs/history/`에 남겨 맥락을 보존함 |

---

## 📂 프로젝트 주요 경로

- **Backend**: `backend/` (Spring Boot)
- **Frontend**: `frontend/` (Next.js)
- **Documents**: `docs/` (Architecture, History, Rules)
