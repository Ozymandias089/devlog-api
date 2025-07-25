# Devlog API - 사용자 인증 시스템

`Devlog API`는 개발자 블로그 시스템의 사용자 인증 및 계정 관리를 위한 Spring Boot 기반 RESTful API입니다.

## ✅ 기술 스택

- Java 24
- Spring Boot 3.x
- Spring Data JPA
- H2 / MySQL (설정에 따라)
- Lombok
- Jakarta Validation
- REST API

---

## 📌 요구사항 (회원가입)

| 항목           | 내용                                                                 |
|----------------|----------------------------------------------------------------------|
| 이메일 중복 검사 | 이미 가입된 이메일로는 회원가입할 수 없습니다.                        |
| UUID 발급       | 회원 고유 식별자로 UUID를 자동 생성합니다.                             |
| Username 자동 생성 | 가입 시 `user-랜덤숫자` 형식의 사용자명을 자동으로 생성합니다.         |
| 비밀번호 처리    | 현재는 평문 저장. 이후 BCrypt 암호화 방식으로 개선 예정입니다.            |

---

## 🚀 회원가입 API 명세서

### `POST /api/members/signup`

회원가입을 위한 API입니다.

#### ✅ Request

```json
POST /api/members/signup
Content-Type: application/json

{
  "email": "example@example.com",
  "password": "plainpassword"
}
```
좋아, 아래는 README.md 파일 예시야.
이 예시는 너의 회원가입 API와 관련된 현재 코드 구조 및 요구사항을 기반으로 작성된 것이며,
앞으로 다른 기능(로그인, 비밀번호 변경 등)이 추가되면 확장 가능하게 작성되어 있어.

---


# Devlog API - 사용자 인증 시스템

`Devlog API`는 개발자 블로그 시스템의 사용자 인증 및 계정 관리를 위한 Spring Boot 기반 RESTful API입니다.

## ✅ 기술 스택

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 / MySQL (설정에 따라)
- Lombok
- Jakarta Validation
- REST API

---

## 📌 요구사항 (회원가입)

| 항목           | 내용                                                                 |
|----------------|----------------------------------------------------------------------|
| 이메일 중복 검사 | 이미 가입된 이메일로는 회원가입할 수 없습니다.                        |
| UUID 발급       | 회원 고유 식별자로 UUID를 자동 생성합니다.                             |
| Username 자동 생성 | 가입 시 `user-랜덤숫자` 형식의 사용자명을 자동으로 생성합니다.         |
| 비밀번호 처리    | 현재는 평문 저장. 이후 BCrypt 암호화 방식으로 개선 예정입니다.            |

---

## 🚀 회원가입 API 명세서

### `POST /api/members/signup`

회원가입을 위한 API입니다.

#### ✅ Request

```json
POST /api/members/signup
Content-Type: application/json

{
  "email": "example@example.com",
  "password": "plainpassword"
}
```

🔐 Validation
	•	email: 이메일 형식 + 필수 값
	•	password: 최소 1자 이상 + 필수 값

✅ Response (201 Created)
```json
{
  "uuid": "ff4a3081-f9d7-4c69-b6a1-48292a3edb11",
  "email": "example@example.com",
  "username": "user-329845"
}
```
❌ Error Response (400 Bad Request)
```json
"This email already exists."
```

---

🛠 프로젝트 구조 (일부)
```
com.ozymandias089.devlog_api
├── user
│   ├── controller
│   │   └── MemberController.java
│   ├── dto
│   │   ├── SignupRequestDTO.java
│   │   └── UserResponseDTO.java
│   ├── entity
│   │   └── Member.java
│   ├── repository
│   │   └── MemberRepository.java
│   └── service
│       └── MemberService.java
├── exception
│   ├── EmailAlreadyExistsException.java
│   └── GlobalExceptionHandler.java
└── ...
```

---

⏭️ TODO
	•	비밀번호 암호화 (BCryptEncoder 등)
	•	로그인 기능
	•	JWT 토큰 발급 및 인증
	•	이메일 인증
	•	프로필 수정
	•	테스트 코드 작성 (단위/통합)

---

💡 기여 & 라이센스

해당 프로젝트는 개인 또는 팀 개발 학습 목적으로 자유롭게 사용 가능합니다.
Pull Request 또는 Issue는 언제든 환영합니다!

---
TODO: 
- AuthenticationFilter 추가
- Token 관련 기능들을 통합
- Spring Security Config 구성
- login/Logout/RefreshToken Controller 구성
- 예외처리 글로벌 핸들러 구성
- 추가 필요 기능
 	1.	회원가입 API + 비밀번호 암호화 저장
	2.	로그인 API → matches()로 비밀번호 검증 추가
	3.	JwtAuthenticationFilter 등록 → 실질적인 인증 작동
	4.	SecurityConfig에서 인증 흐름 구성
	5.	(선택) 예외처리/테스트 작성
