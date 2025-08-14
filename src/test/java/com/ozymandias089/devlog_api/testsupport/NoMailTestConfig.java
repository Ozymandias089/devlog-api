package com.ozymandias089.devlog_api.testsupport;

import com.ozymandias089.devlog_api.member.service.EmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class NoMailTestConfig {
    @Bean
    @Primary
    EmailService noMailEmailService() {
        return new EmailService(null) {
            @Override
            public void sendPasswordResetEmail(String toEmail, String resetURL) {
                // no-op
            }
        };
    }
}
