# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AI Treehole (智能伴学与情绪疏导 AI 树洞系统) — an AI-powered emotional support and study companion for students. Users chat with an AI companion via SSE streaming, with lexicon-based emotion analysis, self-harm risk detection, counselor warning workflows, and study goal tracking. UI and commit messages are in Chinese.

## Common Commands

### Backend (`treehole-server/`)

```bash
cd treehole-server
mvn clean package            # Build
mvn spring-boot:run          # Run (port 8080)
mvn test                     # Run tests
mvn test -Dtest=BcryptHashGeneratorTest   # Run a single test class
```

Entry point: `com.zxw.treehole.TreeholeApplication`

### Frontend (`treehole-web/`)

```bash
cd treehole-web
npm install                  # Install dependencies
npm run dev                  # Dev server (port 5173, proxies /api → localhost:8080)
npm run build                # Production build → dist/
npm run preview              # Preview production build
```

### Infrastructure Requirements

- MySQL 8.0 — database `treehole` on `localhost:3306` (user `root`, password `123456` in dev)
- Redis — `localhost:6379`
- Schema DDL: `treehole-server/src/main/resources/db/schema.sql`
- Seed data: `treehole-server/src/main/resources/db/data-init-auth-sample.sql`

### Demo Accounts (auto-created by `DataInitializer`)

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | admin123 |
| Counselor | counselor | counselor123 |
| Student | student | student123 |

## Architecture

### Two Active Modules

- **`treehole-server/`** — Spring Boot 3.2.5 backend (Java 17, Maven). The only active backend.
- **`treehole-web/`** — Vue 3.4 SPA frontend (Vite 5.2, Element Plus, Pinia, ECharts).
- `backend/` is a vestigial IntelliJ stub — ignore it.

### Backend Package Layout (`com.zxw.treehole`)

- **`ai/`** — AI model abstraction: `ChatLanguageModel` interface, `OpenAiCompatibleChatModel` (SSE streaming), `MockChatLanguageModel`. Persona and safe-reply prompts loaded from `resources/prompts/`.
- **`ai/risk/`** — `RiskAssessmentService`: keyword-based self-harm/suicide detection.
- **`emotion/`** — `EmotionAnalysisEngine` interface, `LexiconEmotionAnalyzer` (dictionary/keyword sentiment scoring).
- **`security/`** — JWT-based stateless auth. `JwtUtil` for token creation/validation, `JwtAuthenticationFilter` for request filtering. RBAC via `@PreAuthorize` with three roles: `STUDENT`, `COUNSELOR`, `ADMIN`.
- **`controller/`** — REST endpoints organized by role: `controller/student/`, `controller/counselor/`, `controller/admin/`, plus `AuthController`.
- **`config/`** — `SecurityConfig` (stateless session, JWT filter chain), `AiChatProperties` (AI provider config), `ChatAsyncConfiguration` (thread pool for SSE streaming: 4 core, 32 max).
- **`bootstrap/`** — `DataInitializer` auto-seeds roles, demo accounts, and default alert rules on startup.
- **`entity/`** — MyBatis-Plus entities. All tables use logical delete (`deleted` field) and `create_time`/`update_time` audit fields.
- **`common/`** — `Result<T>`, `PageResult<T>`, `ResultCode` — unified API response wrappers.

### Key Architectural Flow: AI Chat (SSE)

1. `POST /api/student/chat/stream` → `AiChatStreamServiceImpl` (runs on `chatExecutor` thread pool)
2. Message persisted → `EmotionWarningService.analyzeAndPersist()` runs emotion analysis
3. If risk HIGH: returns `blocked` SSE event with crisis safe-reply from `safe-reply-high-risk.txt`
4. Otherwise: builds context (up to 24 messages history), prepends persona system prompt (with user's AI preferences), streams LLM response as SSE `delta` events
5. Frontend parses SSE via `readSseStream()` utility in `utils/sseStream.js`

### Frontend Architecture

- **State**: Pinia stores — `stores/user.js` (auth + role helpers), `stores/app.js` (UI state), `stores/aiPreferences.js` (AI personality, persisted to localStorage)
- **API layer**: `api/http.js` (Axios instance with JWT interceptor, 401 redirect), `api/chat.js` (SSE streaming), `api/auth.js`, `api/emotion.js`
- **Routing**: `router/index.js` — Vue Router with `beforeEach` guard enforcing JWT + role-based access via route `meta.roles`
- **Views** are organized by role: `views/common/`, `views/student/`, `views/counselor/`, `views/admin/`

### Database (13 tables)

- **Auth**: `sys_user`, `sys_role`, `sys_user_role`
- **Study**: `study_goal`, `study_checkin`
- **AI Chat**: `ai_chat_session`, `ai_chat_message`
- **Emotion/Warning**: `emotion_record`, `warning_record`, `alert_rule`, `intervention_record`
- **System**: `system_log`, `model_config`

### Configuration

- `application.yml` — AI provider settings (currently MiniMax M2.5), JWT secret (`${JWT_SECRET:...}` env var override), MyBatis-Plus, Swagger
- `application-dev.yml` — MySQL/Redis connection, server port 8080
- AI model provider is swappable — `MockChatLanguageModel` available for testing without API calls
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Conventions

- All user-facing text, comments, Javadoc, and commit messages are in Chinese.
- API responses use `Result<T>` wrapper with `ResultCode` enum.
- MyBatis-Plus is used for ORM — prefer mapper methods over raw SQL. Only `StudyCheckinMapper.xml` uses custom XML mapper.
- Lombok is used for boilerplate reduction — entities use `@Data`, `@TableName`, `@TableId(type = IdType.AUTO)`, `@TableLogic`, `@TableField(fill = FieldFill.INSERT)`, etc.
- Frontend uses Vue 3 Composition API exclusively (`<script setup>`).
- Element Plus components with Chinese locale.
