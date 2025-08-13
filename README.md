# Devlog API

Spring Boot 기반의 RESTful API 서버로, 회원 관리 및 인증 기능을 제공합니다.  
JWT 기반 인증과 Redis를 활용한 세션/토큰 관리를 지원하며, Docker Compose로 MySQL과 Redis 환경을 손쉽게 구성할 수 있습니다.

---

## 🚀 기술 스택

**Backend**
- Java 24
- Spring Boot 3.5.4
- Spring Data JPA
- Spring Security
- Spring Validation
- Springdoc OpenAPI (Swagger UI)
- JWT (io.jsonwebtoken)
- Lombok

**Database / Cache**
- MySQL 8.0
- Redis 7.2

**Infra / Build**
- Docker, Docker Compose
- Gradle

---

## 📂 프로젝트 구조

```plaintext
src
 └─ main
     ├─ java/com/ozymandias089/devlog_api
     │   ├─ DevlogApiApplication.java         # Spring Boot 메인 실행 클래스
     │   ├─ global                            # 전역 설정, 공용 유틸, 예외 등
     │   │   ├─ config                        # 전역 설정 클래스
     │   │   │   ├─ RedisConfig.java
     │   │   │   ├─ SecurityConfig.java
     │   │   │   └─ SwaggerConfig.java
     │   │   ├─ enums                         # 전역 Enum
     │   │   │   └─ Role.java
     │   │   ├─ exception                     # 전역 예외 클래스
     │   │   │   ├─ DuplicateEmailExcpetion.java
     │   │   │   ├─ InvalidCredentialsException.java
     │   │   │   └─ JwtValidationException.java
     │   │   └─ util                          # 공용 유틸리티
     │   │       ├─ Functions.java
     │   │       └─ RegexPatterns.java
     │   ├─ member                            # Member(회원) 도메인
     │   │   ├─ controller                    # REST API 컨트롤러
     │   │   │   └─ MemberController.java
     │   │   ├─ dto                           # 요청/응답 DTO
     │   │   │   ├─ request
     │   │   │   │   ├─ LoginRequestDTO.java
     │   │   │   │   ├─ PasswordCheckRequestDTO.java
     │   │   │   │   ├─ PasswordResetConfirmRequestDTO.java
     │   │   │   │   ├─ PasswordResetRequestDTO.java
     │   │   │   │   ├─ PasswordValidationRequestDTO.java
     │   │   │   │   └─ SignupRequestDTO.java
     │   │   │   └─ response
     │   │   │       ├─ LoginResponseDTO.java
     │   │   │       ├─ PasswordValidationResponseDTO.java
     │   │   │       ├─ SignupResponseDTO.java
     │   │   │       └─ UserResponseDTO.java
     │   │   ├─ entity                        # JPA 엔티티
     │   │   │   └─ Member.java
     │   │   ├─ jwt                           # JWT 관련 구성
     │   │   │   ├─ JwtAuthenticationFilter.java
     │   │   │   └─ JwtTokenProvider.java
     │   │   ├─ MemberMapper.java             # DTO ↔ Entity 매핑
     │   │   ├─ repository                    # 데이터 접근 계층
     │   │   │   └─ MemberRepository.java
     │   │   └─ service                       # 비즈니스 로직
     │   │       ├─ EmailService.java
     │   │       └─ MemberService.java
     │   └─ post                              # (추가 구현 예정 도메인)
     └─ resources
         ├─ application.properties            # 환경 설정
         ├─ static                            # 정적 리소스(css, js 등)
         └─ templates                         # Thymeleaf 템플릿
```

---

## 📜 주요 기능
- [ ] 회원가입
- [ ] 로그인 / 로그아웃
- [ ] 비밀번호 재설정
- [ ] 이메일 인증
- [ ] JWT 기반 인증 / 인가
- [ ] Redis를 활용한 토큰 관리

---

## 🛠️ 실행 방법

### 1. Docker Compose 실행
```bash
docker-compose up -d
```
- MySQL, Redis, 애플리케이션 컨테이너가 함께 실행됩니다.
- 기본 포트
    - MySQL: `3306`
    - Redis: `6379`
    - API 서버: `8080`

### 2. 로컬 실행
```bash
./gradlew bootRun
```

### 3. 환경 변수
- `application.properties` 또는 `application.yml`에서 DB, Redis 정보 수정

---

## 📄 API 문서
- Swagger UI: 실행 후 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## 📌 개선 예정
- 게시글(Post) 도메인 구현
- 이메일 인증 로직 강화
- API 예외 응답 표준화
- 통합 테스트 케이스 추가

---