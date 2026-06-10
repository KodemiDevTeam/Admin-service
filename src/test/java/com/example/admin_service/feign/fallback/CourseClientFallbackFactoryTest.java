package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.request.CourseModerationRequest;
import com.example.admin_service.dto.response.CourseResponseDTO;
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

    @Test
    void testReviewCourseWithNullCause() {
        CourseClient client = fallbackFactory.create(null);
        CourseModerationRequest request = new CourseModerationRequest();
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.reviewCourse("token", "courseId", request);
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testGetAllCoursesAdminWithNullCause() {
        CourseClient client = fallbackFactory.create(null);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getAllCoursesAdmin("token");
        });
        
        assertNotNull(exception.getMessage());
    }

    @Test
    void testExceptionIncludesCause() {
        RuntimeException cause = new RuntimeException("Original error");
        CourseClient client = fallbackFactory.create(cause);
        
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            client.getAllCoursesAdmin("token");
        });
        
        assertSame(cause, exception.getCause());
    }

    @Test
    void testReviewCourseExceptionMessage() {
        DownstreamServiceException exception = assertThrows(DownstreamServiceException.class, () -> {
            fallbackClient.reviewCourse("token", "courseId", new CourseModerationRequest());
        });

        assertTrue(exception.getMessage().contains("unavailable"));
    }

    @Test
    void testAllMethodsThrowDownstreamServiceException() {
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.getAllCoursesAdmin("token"));
        assertThrows(DownstreamServiceException.class, () -> fallbackClient.reviewCourse("token", "id", new CourseModerationRequest()));
    }
}
