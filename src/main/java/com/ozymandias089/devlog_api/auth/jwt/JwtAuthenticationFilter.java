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
        String requestURI = request.getRequestURI();
        String bearer = request.getHeader("Authorization");

        // ✅ 인증 필요 없는 경로는 필터 우회
        if (requestURI.startsWith("/api/members") || requestURI.startsWith("/auth") || requestURI.startsWith("/public")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            log.debug("Extracted Token: {}", token);

            if (jwtTokenProvider.isTokenValid(token) && !jwtTokenProvider.isAccessTokenBlacklisted(token)) {
                String subject = jwtTokenProvider.getSubject(token);

                // Role 정보 꺼내기
                String roleStr = jwtTokenProvider.getRoleFromToken(token);

                // Role → GrantedAuthority 변환
                Collection<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + roleStr)
                );

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("Authentication set for subject: {}, role: {}", subject, roleStr);
            } else {
                log.info("Invalid or Blacklisted JWT Token");
            }
        }

        filterChain.doFilter(request, response);
    }}
