package com.ozymandias089.devlog_api.member.api;

import com.ozymandias089.devlog_api.testsupport.ContainersBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MemberApiIT extends ContainersBase {
    @LocalServerPort int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @Test
    void signup_then_login_then_update_username() {
        // 1) 회원가입
        given().contentType(ContentType.JSON).body("""
          {"email":"u@test.com","password":"StrongPwd123!","username":"u"}
        """).when().post("/api/members/signup")
                .then().statusCode(anyOf(is(200), is(201)));

        // 2) 로그인
        var login = given().contentType(ContentType.JSON).body("""
          {"email":"u@test.com","password":"StrongPwd123!"}
        """).when().post("/api/members/login")
                .then().statusCode(200)
                .body("accessToken", not(emptyOrNullString()))
                .body("refreshToken", not(emptyOrNullString()))
                .extract();

        var access = login.path("accessToken");

        // 3) 닉네임 변경(보호 리소스)
        given().header("Authorization", "Bearer " + access)
                .contentType(ContentType.JSON).body(
                        """
                                {"newUsername":"renamed"}"""
                )
                .when().patch("/api/members/update-username")
                .then().statusCode(200);
    }

    @Test
    void password_validate_returns_false_for_weak() {
        given().contentType(ContentType.JSON).body(
                        """
                        {"password":"short"}""")
                .when().post("/api/members/password/validate")
                .then().statusCode(200)
                .body("isValid", is(false));
    }

    @Test
    void protected_endpoint_without_token_is_401() {
        given().when().post("/api/members/logout")
                .then().statusCode(401);
    }
}
