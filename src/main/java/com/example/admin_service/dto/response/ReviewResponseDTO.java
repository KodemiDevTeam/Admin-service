package com.example.admin_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReviewResponseDTO {
    private String reviewId;
    private String courseId;
    private String userId;
    private String reviewerName;
    private String reviewerPhoto;
    private Integer rating;
    private String reviewText;
    private Boolean isVerified;
    private Integer likes;
    private Instant createdAt;
    private Instant updatedAt;
}