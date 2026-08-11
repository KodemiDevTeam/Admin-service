package com.example.admin_service.dto.request;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LessonEntity {
    private String lessonId;
    private String title;
    private List<ContentItem> contentItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentItem {
        private String type;
        private String key;
        private String processedKey;
        private String status;
        private String label;
        private Integer order;
    }
}