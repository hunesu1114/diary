# CLAUDE.md — 단지(Danji) 일기장 웹앱

이 파일은 Claude Code가 이 저장소에서 작업할 때 따라야 할 프로젝트 규칙과 아키텍처 정의입니다.

---

## 1. 프로젝트 개요

개인 일기장 웹 애플리케이션. 날짜·시간·제목·내용으로 일기를 작성하고, 저장/수정/삭제/조회한다.
각 일기에 잠금(lock)을 걸 수 있고, Kakao OAuth2 로그인과 JWT 세션(24시간)을 사용한다.
세련되고 깔끔한 테마에 dark/light 모드를 제공한다.

## 2. 기술 스택

| 영역      | 스택                                                       |
| --------- | ---------------------------------------------------------- |
| Frontend  | Vue 3 + TypeScript + Vite (Pinia, Vue Router, Axios)       |
| Backend   | Java 17 + Spring Boot 3 + Gradle                           |
| DB        | PostgreSQL 16                                              |
| Auth      | Spring Security OAuth2 (Kakao) + JWT (24h)                 |
| Deploy    | Docker Compose (frontend / backend / postgres 일괄 빌드·배포) |

## 3. 디렉터리 구조

```
diary/
├── CLAUDE.md
├── docker-compose.yml        # postgres + backend + frontend 일괄 구동
├── .env.example              # 시크릿 placeholder (복사해 .env 작성)
├── .gitignore
├── backend/                  # Spring Boot 애플리케이션
│   ├── build.gradle
│   ├── settings.gradle
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/danji/diary/
│       │   ├── DiaryApplication.java
│       │   ├── config/        # Security, CORS, JWT 설정
│       │   ├── domain/        # JPA 엔티티 (User, Diary)
│       │   ├── repository/    # Spring Data JPA
│       │   ├── service/       # 비즈니스 로직
│       │   ├── controller/    # REST API
│       │   ├── dto/           # 요청/응답 DTO
│       │   └── security/      # JWT, OAuth2 핸들러
│       └── resources/
│           └── application.yml
└── frontend/                 # Vue 3 SPA
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json
    ├── index.html
    ├── Dockerfile
    ├── nginx.conf            # 정적 서빙 + /api 프록시
    └── src/
        ├── main.ts
        ├── App.vue
        ├── router/
        ├── stores/           # Pinia (auth, theme)
        ├── api/              # axios 인스턴스 + API 모듈
        ├── views/            # 페이지 단위 컴포넌트
        ├── components/       # 재사용 컴포넌트
        └── styles/           # 테마 변수 (dark/light)
```

## 4. 도메인 모델

### User
- `id` (PK), `kakaoId` (unique), `nickname`, `email` (nullable)
- `lockPin` — **계정 공통 PIN**의 해시(BCrypt). 미설정 시 null
- `createdAt`

### Diary
- `id` (PK), `user` (FK → User)
- `title`, `content`
- `diaryDate` (LocalDate), `diaryTime` (LocalTime) — 작성 시 사용자가 입력
- `locked` (boolean, default false)
- `createdAt`, `updatedAt`

## 5. 잠금(Lock) 정책 — 중요

- 잠금은 **계정 공통 PIN 하나**로 동작한다. 일기별 개별 PIN이 아니다.
- PIN은 평문 저장 금지. **BCrypt 해시**로 저장한다.
- 목록/상세 조회 시 `locked = true`인 일기의 `content`(및 민감 필드)는 **서버에서 응답에 포함하지 않는다**(마스킹).
- 잠긴 일기 열람은 `POST /api/diaries/{id}/unlock` 에 PIN을 보내 검증에 성공한 경우에만 전체 내용을 반환한다.
- PIN 미설정 상태에서 잠금 시도 시 먼저 PIN 설정을 요구한다.

## 6. API 설계

인증 필요(JWT Bearer). 기본 prefix `/api`.

| 메서드 | 경로                          | 설명                                 |
| ------ | ----------------------------- | ------------------------------------ |
| GET    | `/oauth2/authorization/kakao` | Kakao 로그인 시작 (Spring 자동 처리) |
| GET    | `/api/me`                     | 내 정보 / PIN 설정 여부              |
| POST   | `/api/me/pin`                 | 계정 공통 PIN 설정/변경              |
| GET    | `/api/diaries`                | 내 일기 목록 (잠금 항목 content 마스킹) |
| GET    | `/api/diaries/{id}`           | 단건 조회 (잠금 시 마스킹)           |
| POST   | `/api/diaries`                | 작성                                 |
| PUT    | `/api/diaries/{id}`           | 수정                                 |
| DELETE | `/api/diaries/{id}`           | 삭제                                 |
| POST   | `/api/diaries/{id}/lock`      | 잠금 설정                            |
| POST   | `/api/diaries/{id}/unlock`    | PIN 검증 후 전체 내용 반환           |

- 모든 일기 API는 **로그인 사용자 본인의 데이터만** 접근 가능(소유권 검증 필수).

## 7. 인증 흐름

1. 프론트의 "Kakao 로그인" → 백엔드 `/oauth2/authorization/kakao` 로 이동
2. Kakao 인증 성공 → 백엔드 OAuth2 success 핸들러가 User upsert 후 **JWT 발급(만료 24h)**
3. 프론트로 토큰 전달(리다이렉트 쿼리 또는 프론트 콜백 페이지). 프론트는 토큰 저장 후 axios `Authorization: Bearer` 헤더로 호출
4. 토큰 만료/무효 시 401 → 로그인 화면으로

## 8. 디자인 / 테마

- 세련되고 깔끔한 미니멀 테마. CSS 변수 기반 토큰(색/간격/라운드/그림자).
- **dark/light 모드** 토글 제공, 선택값은 `localStorage`에 저장하고 시스템 설정(`prefers-color-scheme`)을 초기값으로 사용.
- 컴포넌트는 일관된 spacing scale과 radius 사용. 과한 장식 지양.

## 9. 시크릿 / 환경변수

- 실제 키/비밀번호는 커밋하지 않는다. `.env.example` 의 placeholder를 복사해 `.env` 작성.
- 필요한 값: `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `KAKAO_REDIRECT_URI`, `JWT_SECRET`, `POSTGRES_*`, `APP_FRONTEND_URL`.
- `.env` 와 빌드 산출물은 `.gitignore` 에 포함.

## 10. 개발 / 실행

```bash
# 전체 일괄 빌드 & 구동
docker compose up --build

# 프론트 단독 개발
cd frontend && npm install && npm run dev

# 백엔드 단독 개발
cd backend && ./gradlew bootRun
```

- Frontend: `http://localhost:5173` (dev) / `http://localhost:8080` (compose, nginx)
- Backend API: `http://localhost:8081`
- PostgreSQL: `localhost:5432`

## 11. Claude 작업 규칙

- **불확실한 내용은 반드시 사용자에게 물어보고 진행한다.** 임의 가정 금지.
- 조회(읽기)성 작업을 제외한 모든 작업은 **무슨 작업인지 한 줄로 설명한 뒤** 진행한다.
- 시크릿/키를 코드나 커밋에 하드코딩하지 않는다.
- 잠금 PIN 등 민감 정보는 항상 해시 처리한다.
- 변경 후에는 가능한 한 빌드/타입체크로 검증한다.
