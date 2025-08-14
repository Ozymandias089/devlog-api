package com.ozymandias089.devlog_api.global.config;

import com.ozymandias089.devlog_api.member.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.List;

/**
 * Spring Security 설정 클래스.
 * <p>
 * JWT 기반 인증을 적용하며, Stateless 세션 정책을 사용합니다.
 * CORS, CSRF, 세션 관리, 경로별 접근 제어, 필터 체인 구성을 정의합니다.
 * </p>
 *
 * <ul>
 *     <li>CORS: 모든 Origin 허용 (또는 프론트엔드 URL로 제한 가능)</li>
 *     <li>CSRF: Stateless API이므로 비활성화</li>
 *     <li>SessionCreationPolicy.STATELESS: 세션 저장소를 사용하지 않음</li>
 *     <li>JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록</li>
 * </ul>
 *
 * @author Younghoon Choi
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * SecurityFilterChain을 구성합니다.
     *
     * @param http HttpSecurity 빌더 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 설정 중 오류 발생 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration c = new CorsConfiguration();
                    // ★ 자격증명을 쓸 경우 프런트 도메인을 명시하세요.
                    c.setAllowedOrigins(List.of(
                            "https://your-frontend.example.com",
                            "https://staging-frontend.example.com"
                            // 로컬 개발 시: "http://localhost:5173" 등 프로필별 yml 권장
                    ));
                    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                    c.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
                    c.setExposedHeaders(List.of("Authorization"));
                    c.setAllowCredentials(true); // 자격증명 사용 시 * 금지
                    c.setMaxAge(3600L);
                    return c;
                }))
                // CSRF 비활성화(토큰 기반)
                .csrf(AbstractHttpConfigurer::disable)
                // 세션 Stateless
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 경로 권한
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/public/**",
                                "/api/members/signup",
                                // Swagger/OpenAPI 허용 (필요 시)
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                // JWT 필터
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * (프록시 종단 시 권장) X-Forwarded-* / Forwarded 헤더 신뢰를 위한 필터.
     * Nginx/ALB 뒤에서 HTTPS 강제 로직이 올바르게 동작하도록 도와줍니다.
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    /**
     * 인증 관리자 빈 생성
     *
     * @param config AuthenticationConfiguration 객체
     * @return AuthenticationManager 인스턴스
     * @throws Exception 인증 매니저 생성 실패 시
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 비밀번호 암호화를 위한 PasswordEncoder 빈 생성
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 운영 환경에서만 HTTPS를 강제하고 HSTS를 적용합니다.
     * 프록시 뒤에 있을 경우 X-Forwarded-Proto=https 헤더가 필요합니다.
     */
    @Bean
    @Profile("prod")
    public SecurityFilterChain httpsEnforcedChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .requiresChannel(channel -> channel
                        .anyRequest().requiresSecure()
                )
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000)
                        )
                );
        return http.build();
    }

}
