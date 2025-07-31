package com.ozymandias089.devlog_api.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("▶ Incoming request: {}", request.getRequestURI());  // 추가
        String requestURI = request.getRequestURI();
        String bearerToken = request.getHeader("Authorization");

        // ⛔ 인증 예외 경로: 정확히 필요한 URI만 허용
        if (isPermitAllPath(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            try {
                if (jwtTokenProvider.isTokenValid(token) && !jwtTokenProvider.isAccessTokenBlacklisted(token)) {
                    String subject = jwtTokenProvider.getSubject(token);
                    String role = jwtTokenProvider.getRoleFromToken(token);

                    Collection<GrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(subject, null, authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("[✔️ 인증 성공] Subject: {}, Role: {}", subject, role);
                } else {
                    log.warn("[⚠️ 인증 실패] 유효하지 않거나 블랙리스트된 토큰입니다.");
                }
            } catch (Exception e) {
                log.error("[🔥 인증 처리 중 예외 발생] {}", e.getMessage(), e);
                // 필요하다면 아래처럼 인증 오류 응답을 보낼 수도 있음
                // response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            }
        } else {
            log.debug("[🔒 인증 헤더 없음] Authorization 헤더가 존재하지 않거나 Bearer 토큰이 아닙니다.");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 인증 없이 접근 가능한 경로인지 확인하는 메서드
     */
    private boolean isPermitAllPath(String uri) {
        return uri.equals("/api/members/signup") ||
                uri.startsWith("/auth/") ||
                uri.startsWith("/public/");
    }
}
