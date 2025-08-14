package com.ozymandias089.devlog_api.member.api;

import com.ozymandias089.devlog_api.testsupport.ContainersBase;
import com.ozymandias089.devlog_api.testsupport.NoMailTestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.frontend.password-reset-url=http://localhost:3000/password-reset",
                "spring.jpa.hibernate.ddl-auto=update",
                "springdoc.api-docs.enabled=false",
                "springdoc.swagger-ui.enabled=false"
        }
)
@ActiveProfiles("test")
@Import(NoMailTestConfig.class)
public class PasswordResetFlowIT extends ContainersBase {
    @LocalServerPort int port;

    @MockitoBean
    JavaMailSender mailSender; // 테스트 중 메일 전송 무력화

    private String email;

    @BeforeEach
    void setup() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        // 메일 스텁: 실제 전송 방지
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
        doNothing().when(mailSender).send(any(MimeMessage.class));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // 매 테스트마다 유니크 이메일 생성(중복 가입 방지)
        email = "p+" + UUID.randomUUID() + "@test.com";

        // 사전 회원가입
        given().contentType(ContentType.JSON).body("""
          {"email":"%s","password":"StrongPwd123!","username":"p"}
        """.formatted(email))
                .when().post("/api/members/signup")
                .then().statusCode(anyOf(is(200), is(201)));
    }

    @Test
    void issue_verify_confirm_reset() {
        // 로그인
        var login = given().contentType(ContentType.JSON).body("""
          {"email":"%s","password":"StrongPwd123!"}
        """.formatted(email))
                .when().post("/api/members/login")
                .then().statusCode(200)
                .body("accessToken", not(emptyOrNullString()))
                .extract();

        var access = login.path("accessToken");

        // 1) 로그인 상태에서 재설정 토큰 발급
        var issued = given().header("Authorization", "Bearer " + access)
                .contentType(ContentType.JSON).body("""
                        {"password":"StrongPwd123!"}""")
                .when().post("/api/members/password-reset/issue")
                .then().statusCode(200)
                .body("resetToken", not(emptyOrNullString()))
                .extract();

        var resetToken = issued.path("resetToken");

        // 2) 토큰 검증 (boolean 본문)
        given().queryParam("resetToken", resetToken)
                .when().get("/api/members/password-reset/verify")
                .then().statusCode(200)
                .body(is(true)); // 또는 .body(equalTo(true))

        // 3) 재설정 확정
        given().contentType(ContentType.JSON).body("""
          { "resetToken": "%s", "newPassword":"AnotherStrongPwd456!" }
        """.formatted(resetToken))
                .when().post("/api/members/password-reset/confirm")
                .then().statusCode(anyOf(is(200), is(204)));
    }
}
