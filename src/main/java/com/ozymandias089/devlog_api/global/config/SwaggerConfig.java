package com.ozymandias089.devlog_api.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 설정 클래스.
 * <p>
 * API 문서화를 위해 OpenAPI 3.0 스펙 기반의 Swagger UI를 구성합니다.
 * </p>
 *
 * <ul>
 *     <li>API 제목, 설명, 버전 정보 설정</li>
 *     <li>Swagger UI를 통해 API 테스트 가능</li>
 * </ul>
 *
 * @author Younghoon Choi
 * @since 1.0
 */
@Configuration
public class SwaggerConfig {
    /**
     * OpenAPI 인스턴스를 구성합니다.
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Devlog API")
                        .description("API Documentation for Devlog Project")
                        .version("1.0.0")
                );
    }
}
