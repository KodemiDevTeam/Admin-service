package com.example.admin_service.dto.response;

import org.junit.jupiter.api.Test;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class CourseResponseDTOTest {

    @Test
    void testGettersSettersAndBuilder() {
        Date now = new Date();
        CourseResponseDTO dto = CourseResponseDTO.builder()
                .courseId("c1")
                .courseCode("C-101")
                .slug("c-101")
                .version(1)
                .createdAt(now)
                .updatedAt(now)
                .createdBy("admin")
                .build();

        assertEquals("c1", dto.getCourseId());
        assertEquals("C-101", dto.getCourseCode());
        assertEquals("c-101", dto.getSlug());
        assertEquals(1, dto.getVersion());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertEquals("admin", dto.getCreatedBy());

        dto.setCourseId("c2");
        assertEquals("c2", dto.getCourseId());
    }
}
