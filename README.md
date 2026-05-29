# 단지 · 일기장

날짜·시간·제목·내용으로 일기를 쓰고 저장/수정/삭제/조회하며, 항목별 잠금(계정 공통 PIN)과 Kakao 로그인(JWT 24h), dark/light 테마를 제공하는 웹앱입니다.

- **Frontend** — Vue 3 + TypeScript + Vite
- **Backend** — Java 17 + Spring Boot 3 + Gradle
- **DB** — PostgreSQL 16
- **Deploy** — Docker Compose

프로젝트 규칙·아키텍처·API 명세는 [CLAUDE.md](CLAUDE.md) 를 참고하세요.

## 빠른 시작 (Docker Compose)

```bash
cp .env.example .env      # 값(특히 KAKAO_*, JWT_SECRET, POSTGRES_PASSWORD) 채우기
docker compose up --build
```

- 앱: http://localhost:8080
- API: http://localhost:8081
- DB: localhost:5432

## 개발 모드

```bash
# Frontend
cd frontend && npm install && npm run dev      # http://localhost:5173

# Backend (gradle 설치 필요)
cd backend && gradle bootRun                    # http://localhost:8081
```

## Kakao 로그인 설정

[Kakao Developers](https://developers.kakao.com) 에서 앱 생성 후:

1. REST API 키 → `KAKAO_CLIENT_ID`
2. (선택) 보안 Client Secret → `KAKAO_CLIENT_SECRET`
3. Redirect URI 등록: `http://localhost:8081/login/oauth2/code/kakao`
4. 동의 항목: 닉네임, 이메일

값은 모두 `.env` 에 넣으며 커밋하지 않습니다.
