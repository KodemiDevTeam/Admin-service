package com.example.admin_service.dto.response;

import org.junit.jupiter.api.Test;
import java.util.Date;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CourseResponseDTOTest {

    @Test
    void testBuilderAndBasicFields() {
        Date now = new Date();
        LessonResponseDTO lesson = new LessonResponseDTO();
        lesson.setVideoKey("videos/lesson.mp4");

        CourseResponseDTO dto = createDto(now, lesson);

        assertAll(
                () -> assertEquals("c1", dto.getCourseId()),
                () -> assertEquals("creator1", dto.getCreatorId()),
                () -> assertEquals("John Doe", dto.getCreatorName()),
                () -> assertEquals("cat1", dto.getCategoryId()),
                () -> assertEquals("Programming", dto.getCategoryName()),
                () -> assertEquals("PENDING", dto.getStatus()),
                () -> assertFalse(dto.getIsVerified()),
                () -> assertEquals("Spring Boot Guide", dto.getTitle()),
                () -> assertEquals("Learn Spring Boot", dto.getDescription()),
                () -> assertEquals("English", dto.getLanguage()),
                () -> assertEquals("Beginner", dto.getSkillLevel()),
                () -> assertEquals("https://thumb.url", dto.getThumbnailUrl())
        );
    }

    @Test
    void testRemainingFields() {
        Date now = new Date();
        LessonResponseDTO lesson = new LessonResponseDTO();
        lesson.setVideoKey("videos/lesson.mp4");

        CourseResponseDTO dto = createDto(now, lesson);

        assertAll(
                () -> assertEquals("https://video.url", dto.getDemoVideoUrl()),
                () -> assertEquals(99.99, dto.getPrice()),
                () -> assertEquals(10, dto.getLessonCount()),
                () -> assertEquals("5 hours", dto.getDurationLabel()),
                () -> assertEquals(4.8, dto.getAverageRating()),
                () -> assertEquals(20, dto.getTotalReviews()),
                () -> assertEquals("Welcome!", dto.getWelcomeMessage()),
                () -> assertEquals("Instructor", dto.getInstructorName()),
                () -> assertEquals("photo", dto.getInstructorPhoto()),
                () -> assertEquals("Title", dto.getInstructorTitle()),
                () -> assertEquals("Bio", dto.getInstructorBio()),
                () -> assertEquals("Free", dto.getCourseType()),
                () -> assertEquals(now, dto.getCreatedAt()),
                () -> assertEquals(now, dto.getUpdatedAt()),
                () -> assertNotNull(dto.getLessons())
        );
    }

    @Test
    void testMaskVideoContent() {
        Date now = new Date();
        LessonResponseDTO lesson = new LessonResponseDTO();
        lesson.setVideoKey("videos/lesson.mp4");

        CourseResponseDTO dto = createDto(now, lesson);

        dto.maskVideoContent();

        assertNull(dto.getLessons().get(0).getVideoKey());
    }

    private CourseResponseDTO createDto(Date now, LessonResponseDTO lesson) {
        return CourseResponseDTO.builder()
                .courseId("c1")
                .creatorId("creator1")
                .creatorName("John Doe")
                .categoryId("cat1")
                .categoryName("Programming")
                .status("PENDING")
                .isVerified(false)
                .title("Spring Boot Guide")
                .description("Learn Spring Boot")
                .language("English")
                .skillLevel("Beginner")
                .thumbnailUrl("https://thumb.url")
                .demoVideoUrl("https://video.url")
                .price(99.99)
                .lessonCount(10)
                .durationLabel("5 hours")
                .averageRating(4.8)
                .totalReviews(20)
                .welcomeMessage("Welcome!")
                .instructorName("Instructor")
                .instructorPhoto("photo")
                .instructorTitle("Title")
                .instructorBio("Bio")
                .courseType("Free")
                .createdAt(now)
                .updatedAt(now)
                .lessons(List.of(lesson))
                .build();
    }
}
