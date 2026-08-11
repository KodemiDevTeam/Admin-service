package com.example.admin_service.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponseDTO {

    private List<LessonResponseDTO> lessons;

    public void maskVideoContent() {
        if (this.lessons != null) {
            for (LessonResponseDTO lesson : this.lessons) {
                if (lesson != null) {
                    lesson.maskContent();
                }
            }
        }
    }
}
