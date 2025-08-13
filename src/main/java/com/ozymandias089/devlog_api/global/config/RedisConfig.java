package com.ozymandias089.devlog_api.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 설정 클래스.
 * <p>
 * Spring Data Redis를 사용하여 애플리케이션과 Redis 서버 간의 연결을 설정합니다.
 * <ul>
 *     <li>{@link LettuceConnectionFactory}를 사용해 RedisConnectionFactory를 구성</li>
 *     <li>{@link StringRedisTemplate} 빈을 생성하여 문자열 기반 Redis 작업 지원</li>
 * </ul>
 * </p>
 *
 * <p>
 * Redis는 JWT 블랙리스트, Refresh Token 저장, 비밀번호 재설정 토큰 관리 등에 사용됩니다.
 * </p>
 *
 * @author Younghoon Choi
 * @since 1.0
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * Redis 연결 팩토리 생성
     *
     * @return RedisConnectionFactory 인스턴스
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }
    /**
     * 문자열 기반 Redis 작업을 위한 템플릿 빈 생성
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return StringRedisTemplate 인스턴스
     */
    @Bean
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
