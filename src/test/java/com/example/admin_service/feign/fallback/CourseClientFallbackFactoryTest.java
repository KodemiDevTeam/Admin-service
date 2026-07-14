package com.example.admin_service.feign.fallback;

import com.example.admin_service.dto.request.CourseModerationRequest;
import com.example.admin_service.exceptions.DownstreamServiceException;
import com.example.admin_service.feign.CourseClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseClientFallbackFactoryTest {

    private CourseClient courseClient;

    @BeforeEach
    void setUp() {
        CourseClientFallbackFactory factory = new CourseClientFallbackFactory();
        courseClient = factory.create(new RuntimeException("Course Service Down"));
    }

    @Test
    void reviewCourse_shouldThrowDownstreamServiceException() {

        CourseModerationRequest request = new CourseModerationRequest();

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> courseClient.reviewCourse(
                        "Bearer token",
                        "course-1",
                        request)
        );

        assertEquals(
                "Course service is currently unavailable.",
                exception.getMessage()
        );
    }

    @Test
    void getAllCoursesAdmin_shouldThrowDownstreamServiceException() {

        DownstreamServiceException exception = assertThrows(
                DownstreamServiceException.class,
                () -> courseClient.getAllCoursesAdmin("Bearer token")
        );

        assertEquals(
                "Course service is currently unavailable.",
                exception.getMessage()
        );
    }

    @Test
    void create_shouldReturnCourseClientFallback() {

        CourseClientFallbackFactory factory = new CourseClientFallbackFactory();

        CourseClient client = factory.create(new RuntimeException("Service Down"));

        assertNotNull(client);
    }
}