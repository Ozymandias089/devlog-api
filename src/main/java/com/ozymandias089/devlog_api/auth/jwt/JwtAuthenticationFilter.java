package com.ozymandias089.devlog_api.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
        String bearer = request.getHeader("Authorization");

        if(bearer != null && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            log.debug("Extracted Token: {}", token);

            // 1. Validate Token
            if (jwtTokenProvider.isTokenValid(token)) {
                // 2. Check Blacklist
                if (!jwtTokenProvider.isAccessTokenBlacklisted(token)) {
                    String subject = jwtTokenProvider.getSubject(token);

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(subject, null, null);
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("Authentication set for subject: {}", subject);
                } else {
                    log.info("Access Token is Blacklisted");
                }
            } else {
                log.info("Invalid JWT Token");
            }
        }

        filterChain.doFilter(request, response);
    }
}
