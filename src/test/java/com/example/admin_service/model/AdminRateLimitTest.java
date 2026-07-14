package com.example.admin_service.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminRateLimitTest {

    @Test
    void testGettersAndSetters() {
        AdminRateLimit adminRateLimit = new AdminRateLimit();

        adminRateLimit.setAdminIdDate("admin123_2026-07-14");
        adminRateLimit.setBroadcastCount(5);

        assertEquals("admin123_2026-07-14", adminRateLimit.getAdminIdDate());
        assertEquals(5, adminRateLimit.getBroadcastCount());
    }

    @Test
    void testEqualsHashCodeAndToString() {
        AdminRateLimit obj1 = new AdminRateLimit();
        obj1.setAdminIdDate("admin123_2026-07-14");
        obj1.setBroadcastCount(5);

        AdminRateLimit obj2 = new AdminRateLimit();
        obj2.setAdminIdDate("admin123_2026-07-14");
        obj2.setBroadcastCount(5);

        assertEquals(obj1, obj2);
        assertEquals(obj1.hashCode(), obj2.hashCode());
        assertTrue(obj1.toString().contains("admin123_2026-07-14"));
    }
}