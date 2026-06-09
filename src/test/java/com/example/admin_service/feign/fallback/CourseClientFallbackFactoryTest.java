package com.example.admin_service.feign.fallback;

import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.CourseClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseClientFallbackFactoryTest {

    private CourseClientFallbackFactory fallbackFactory;
    private CourseClient fallbackClient;

    @BeforeEach
    void setUp() {
        fallbackFactory = new CourseClientFallbackFactory();
        fallbackClient = fallbackFactory.create(new RuntimeException("Service unavailable"));
    }

    @Test
    void testGetAllCoursesAdminThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.getAllCoursesAdmin("token");
        });
    }

    @Test
    void testReviewCourseThrowsDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.reviewCourse("token", "courseId", null);
        });
    }

    @Test
    void testFallbackClientImplementsCourseClient() {
        assertTrue(fallbackClient instanceof CourseClient);
    }

    @Test
    void testFallbackFactoryCreateWithDifferentCauses() {
        CourseClient client1 = fallbackFactory.create(new RuntimeException("Error 1"));
        CourseClient client2 = fallbackFactory.create(new IllegalStateException("Error 2"));

        assertNotNull(client1);
        assertNotNull(client2);
    }

    @Test
    void testMultipleFallbackCreations() {
        CourseClient fallback1 = fallbackFactory.create(new RuntimeException("Error 1"));
        CourseClient fallback2 = fallbackFactory.create(new RuntimeException("Error 2"));

        assertNotNull(fallback1);
        assertNotNull(fallback2);
        assertNotSame(fallback1, fallback2);
    }

    @Test
    void testFallbackWithNullCause() {
        CourseClient client = fallbackFactory.create(null);
        assertNotNull(client);
        assertThrows(DownstreamServiceException.class, () -> client.getAllCoursesAdmin("token"));
    }
}
