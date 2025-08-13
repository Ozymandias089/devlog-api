package com.ozymandias089.devlog_api.global.config;

import com.ozymandias089.devlog_api.member.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                    corsConfig.setAllowedOrigins(List.of("*")); // 또는 프론트엔드 URL로 제한
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("*"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/public/**", "/api/members/signup")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                ).addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class).build();
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
}
