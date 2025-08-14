package com.ozymandias089.devlog_api.testsupport;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ContainersBase {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL =
            new MySQLContainer<>("mysql:8.4")
                    .withDatabaseName("testdb")
                    .withUsername("tester")
                    .withPassword("test")
                    .waitingFor(Wait.forLogMessage(".*ready for connections.*\\s", 1))
                    .withStartupTimeout(Duration.ofMinutes(3));

    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7.2")
                    .withExposedPorts(6379)
                    .waitingFor(Wait.forListeningPort())
                    .withStartupTimeout(Duration.ofMinutes(2));

//    @BeforeAll
//    void start() {}
//
//    @DynamicPropertySource
//    static void registerProps(DynamicPropertyRegistry r) {
//        // --- MySQL
//        r.add("spring.datasource.url", () -> {
//            if (!MYSQL.isRunning()) MYSQL.start();   // ✅ 아직 안 떴다면 즉시 기동
//            return MYSQL.getJdbcUrl();
//        });
//        r.add("spring.datasource.username", () -> {
//            if (!MYSQL.isRunning()) MYSQL.start();
//            return MYSQL.getUsername();
//        });
//        r.add("spring.datasource.password", () -> {
//            if (!MYSQL.isRunning()) MYSQL.start();
//            return MYSQL.getPassword();
//        });
//        r.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
//
//        // --- Redis
//        r.add("spring.data.redis.host", () -> {
//            if (!REDIS.isRunning()) REDIS.start();   // ✅ 아직 안 떴다면 즉시 기동
//            return REDIS.getHost();
//        });
//        r.add("spring.data.redis.port", () -> {
//            if (!REDIS.isRunning()) REDIS.start();
//            return REDIS.getMappedPort(6379);
//        });
//
//        // (선택) JPA 전략
//        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");    }
}
