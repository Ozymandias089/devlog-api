# Devlog API

Spring Boot 기반의 블로그/회원 관리 REST API 입니다. MySQL, Redis, JWT 인증을 사용하며
OpenAPI(Swagger UI) 문서화, Docker/Docker Compose를 지원합니다.

- **Java**: 24 (Gradle toolchain)
- **Spring Boot**: 3.5.4
- **DB**: MySQL 8.4
- **Cache/Token/Blacklist**: Redis 7.2
- **Auth**: JWT (jjwt 0.12.x)
- **API Docs**: springdoc-openapi (Swagger UI)

---

## ✨ 주요 기능

### Members
- 회원가입 / 이메일 중복 체크 / 비밀번호 유효성 검사
- 로그인(Access/Refresh 발급), 로그아웃(Refresh 삭제 + Access 블랙리스트)
- 닉네임(Username) 변경
- 회원 탈퇴
- 비밀번호 재설정: 요청 → 토큰 발급(로그인 상태) → 토큰 검증 → 비밀번호 확정

### Posts
- 생성(Create) — 201 Created + Location: `/api/posts/{slug}`
- 목록(Read list) — 페이지네이션(최대 20), 최신순
- 상세(Read detail) — 조회수 원자적 +1 후 작성자 정보 포함 반환
- 부분 수정(Update, PATCH) — 처리 후 **303 See Other** + Location: `/api/posts/{slug}`
- 삭제(Delete) — 처리 후 **303 See Other** + Location: 목록 URI

---

## 🗂️ 프로젝트 구조

```text
.
├── .DS_Store
├── .gitattributes
├── .gitignore
├── build.gradle
├── docker-compose.yml
├── Dockerfile
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── HELP.md
├── README.md
├── redis.conf
├── settings.gradle
└── src
    ├── .DS_Store
    └── main
        ├── java
        │   └── com
        │       └── ozymandias089
        │           └── devlog_api
        │               ├── DevlogApiApplication.java
        │               ├── global
        │               │   ├── config
        │               │   │   ├── JpaConfig.java
        │               │   │   ├── RedisConfig.java
        │               │   │   ├── SecurityConfig.java
        │               │   │   └── SwaggerConfig.java
        │               │   ├── enums
        │               │   │   └── Role.java
        │               │   ├── exception
        │               │   │   ├── DuplicateEmailExcpetion.java
        │               │   │   ├── ForbiddenActionException.java
        │               │   │   ├── InvalidCredentialsException.java
        │               │   │   ├── JwtValidationException.java
        │               │   │   └── PostNotFoundException.java
        │               │   └── util
        │               │       ├── Functions.java
        │               │       ├── RegexPatterns.java
        │               │       └── SlugUtil.java
        │               ├── member
        │               │   ├── controller
        │               │   │   └── MemberController.java
        │               │   ├── dto
        │               │   │   ├── request
        │               │   │   └── response
        │               │   ├── entity
        │               │   │   └── MemberEntity.java
        │               │   ├── jwt
        │               │   │   ├── JwtAuthenticationFilter.java
        │               │   │   └── JwtTokenProvider.java
        │               │   ├── PasswordValidationResult.java
        │               │   ├── provider
        │               │   │   ├── MemberMapper.java
        │               │   │   └── MemberProvider.java
        │               │   ├── repository
        │               │   │   └── MemberRepository.java
        │               │   └── service
        │               │       ├── EmailService.java
        │               │       └── MemberService.java
        │               └── post
        │                   ├── controller
        │                   │   └── PostController.java
        │                   ├── dto
        │                   │   ├── PostSummaryDTO.java
        │                   │   ├── request
        │                   │   └── response
        │                   ├── entity
        │                   │   └── PostEntity.java
        │                   ├── provider
        │                   │   ├── PostMapper.java
        │                   │   └── SlugProvider.java
        │                   ├── repository
        │                   │   └── PostRepository.java
        │                   └── service
        │                       └── PostService.java
        └── resources
            ├── application.properties
            ├── static
            └── templates
```

> 트리 재생성 팁(맥/리눅스):
> ```bash
> tree -L 9 -a -I 'project-structure.txt|.git|.idea|.gradle|.vscode|build|target|out|dist|logs|*.iml' > TREE.md
> ```
> Windows PowerShell :
> ```bat
> tree /F /A > TREE.txt
> ```

---

## 🧰 기술 스택

- **Spring Boot Starters**
    - actuator, web, validation, security, data-jpa, data-redis, mail
- **DB Driver**: `com.mysql:mysql-connector-j`
- **JWT**: `io.jsonwebtoken:jjwt-* 0.12.6`
- **OpenAPI UI**: `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9`
- **Lombok**, **DevTools**

`build.gradle`의 주요 설정:
```gradle
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(24)
  }
}
```

---

## 🚀 실행 방법

### 1) Docker Compose로 한번에
> 최초 빌드 전에 애플리케이션 JAR이 필요합니다.

```bash
# 1) JAR 빌드
./gradlew bootJar

# 2) 컨테이너 빌드 & 실행
docker compose up -d --build

# 3) 로그 보기
docker compose logs -f app
```

- 애플리케이션: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

**docker-compose.yml** 주요 환경 변수:
```yaml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/devlog
SPRING_DATASOURCE_DRIVER: com.mysql.cj.jdbc.Driver
SPRING_DATASOURCE_USERNAME: devuser
SPRING_DATASOURCE_PASSWORD: devpass
SPRING_REDIS_HOST: redis
SPRING_REDIS_PORT: 6379
```

### 2) 로컬에서 직접 실행
- MySQL 8.4, Redis 7.2 실행 (또는 Docker 컨테이너 사용)
- `src/main/resources/application.properties`에 접속 정보 설정
- 실행:
```bash
./gradlew bootRun
```

---

## 🔌 API 요약 (일부)

### Posts
- `POST /api/posts/create` — 게시글 생성 (201 Created + Location)
- `GET /api/posts/post-list?page=0&size=20` — 목록 조회
- `GET /api/posts/{slug}` — 상세 조회(조회수 +1)
- `PATCH /api/posts/{slug}` — 게시글 부분 수정 → **303 See Other** (`Location: /api/posts/{slug}`)
- `DELETE /api/posts/{slug}` — 게시글 삭제 → **303 See Other** (`Location: /api/posts/post-list?...`)

### Members
- `POST /api/members/signup` — 회원가입
- `GET /api/members/check-email?email=...` — 이메일 중복 체크
- `POST /api/members/password/validate` — 비밀번호 유효성 검사
- `POST /api/members/login` — 로그인(토큰 발급)
- `POST /api/members/logout` — 로그아웃(토큰 무효화)
- `DELETE /api/members/unregister` — 회원 탈퇴
- `PATCH /api/members/update-username` — 닉네임 변경
- Password Reset Flow:
    - `POST /api/members/password-reset/request`
    - `POST /api/members/password-reset/issue` (인증 상태)
    - `GET  /api/members/password-reset/verify?resetToken=...`
    - `POST /api/members/password-reset/confirm`

---

## 🐳 Dockerfile 요약

```dockerfile
FROM openjdk:24-jdk
WORKDIR /app
COPY build/libs/devlog-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

> 주의: JAR 파일명은 버전에 따라 달라질 수 있습니다. `bootJar` 출력 파일명과 Dockerfile의 `COPY` 경로를 맞춰주세요.

---

## ⚙️ 설정 팁
- 프록시(Nginx/ELB) 뒤에서 절대 URL이 필요하면 Spring에서 `ForwardedHeaderFilter` 또는
  `server.forward-headers-strategy=framework` 설정을 사용하세요.
- JPA 감사/감사시간, 슬러그 유일성 제약(UNIQUE INDEX), 정렬 안정화(`createdAt DESC, id DESC`) 권장.

---

## 📜 라이선스
사내/개인 프로젝트 정책에 따라 추가하세요.
