package com.example.admin_service.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.example.admin_service.model.AdminRateLimit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminRateLimitRepositoryTest {

    private DynamoDBMapper dynamoDBMapper;
    private AdminRateLimitRepository repository;

    @BeforeEach
    void setup() {
        dynamoDBMapper = mock(DynamoDBMapper.class);
        repository = new AdminRateLimitRepository(dynamoDBMapper);
    }

    @Test
    void getAdminRateLimit_returnsObject() {
        AdminRateLimit rateLimit = new AdminRateLimit();
        when(dynamoDBMapper.load(AdminRateLimit.class, "admin-1_2026-07-14")).thenReturn(rateLimit);

        AdminRateLimit result = repository.getAdminRateLimit("admin-1_2026-07-14");

        assertSame(rateLimit, result);
    }

    @Test
    void getAdminRateLimit_notFound_returnsNull() {
        when(dynamoDBMapper.load(AdminRateLimit.class, "missing")).thenReturn(null);

        assertNull(repository.getAdminRateLimit("missing"));
    }

    @Test
    void save_callsDynamoDBMapperSave() {
        AdminRateLimit rateLimit = new AdminRateLimit();
        rateLimit.setAdminIdDate("admin-1_2026-07-14");
        rateLimit.setBroadcastCount(3);

        repository.save(rateLimit);

        verify(dynamoDBMapper).save(rateLimit);
    }
}
