package com.ozymandias089.devlog_api.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // 선택: 시간 기준을 UTC로 고정하고 싶을 때
    @Bean
    public Clock clock() { return Clock.systemUTC(); }

    @Bean
    public DateTimeProvider dateTimeProvider(Clock clock) {
        return () -> Optional.of(Instant.now(clock));
    }
}
