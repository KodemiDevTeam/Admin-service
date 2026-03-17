package com.example.admin_service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Builder
@Getter
@Setter
public class CourseResponseDTO {
    private String courseId;
    private String courseCode;
    private String slug;
    private Integer version;
    private Date createdAt;
    private Date updatedAt;
    private String createdBy;
}
