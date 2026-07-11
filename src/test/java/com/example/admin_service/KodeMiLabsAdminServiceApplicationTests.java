package com.example.admin_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
        "JWT_SECRET=test-secret-key-for-unit-testing-only-minimum-32-chars",
        "MAIL_USERNAME=test@test.com",
        "MAIL_PASSWORD=test-password",
        "EUREKA_URL=http://localhost:8761/eureka",
        "AWS_REGION=us-east-1",
        "S3_BUCKET_NAME=test-bucket",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
class KodeMiLabsAdminServiceApplicationTests {

    // Mock both Redis factory types so RedisConfig and RedisReactiveAutoConfiguration
    // can wire their beans without a real Redis connection
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    private ReactiveRedisConnectionFactory reactiveRedisConnectionFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext);
    }

}
