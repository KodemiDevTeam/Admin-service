package com.example.admin_service.dto.request;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LessonEntityTest {

    @Test
    void testNoArgsConstructorAndSetters() {
        LessonEntity lesson = new LessonEntity();
        Date now = new Date();

        lesson.setLessonId("l1");
        lesson.setModuleId("m1");
        lesson.setTitle("Intro to Java");
        lesson.setDescription("Basic Java");
        lesson.setDuration(60);
        lesson.setOrderIndex(1);
        lesson.setLessonType("VIDEO");
        lesson.setLiveSessionId("live-1");
        lesson.setScheduledAt(now);
        lesson.setVideoKey("vid-key");
        lesson.setCreatedAt(now);
        lesson.setUpdatedAt(now);

        assertEquals("l1", lesson.getLessonId());
        assertEquals("m1", lesson.getModuleId());
        assertEquals("Intro to Java", lesson.getTitle());
        assertEquals("Basic Java", lesson.getDescription());
        assertEquals(60, lesson.getDuration());
        assertEquals(1, lesson.getOrderIndex());
        assertEquals("VIDEO", lesson.getLessonType());
        assertEquals("live-1", lesson.getLiveSessionId());
        assertEquals(now, lesson.getScheduledAt());
        assertEquals("vid-key", lesson.getVideoKey());
        assertEquals(now, lesson.getCreatedAt());
        assertEquals(now, lesson.getUpdatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        Date now = new Date();
        LessonEntity lesson = new LessonEntity(
                "l1", "m1", "Title", "Desc", 30, 2,
                null, now, now, "LIVE", "live-2", now, "v-key"
        );

        assertEquals("l1", lesson.getLessonId());
        assertEquals("Title", lesson.getTitle());
        assertEquals(30, lesson.getDuration());
    }

    @Test
    void testContentItemNoArgsConstructor() {
        LessonEntity.ContentItem item = new LessonEntity.ContentItem();
        item.setType("VIDEO");
        item.setKey("k1");
        item.setProcessedKey("pk1");
        item.setStatus("READY");
        item.setLabel("Intro");
        item.setOrder(1);

        assertEquals("VIDEO", item.getType());
        assertEquals("k1", item.getKey());
        assertEquals("pk1", item.getProcessedKey());
        assertEquals("READY", item.getStatus());
        assertEquals("Intro", item.getLabel());
        assertEquals(1, item.getOrder());
    }

    @Test
    void testContentItemAllArgsConstructor() {
        LessonEntity.ContentItem item = new LessonEntity.ContentItem(
                "PDF", "key1", "pkey1", "PROCESSED", "Doc", 2
        );

        assertEquals("PDF", item.getType());
        assertEquals("key1", item.getKey());
        assertEquals("pkey1", item.getProcessedKey());
        assertEquals("PROCESSED", item.getStatus());
        assertEquals("Doc", item.getLabel());
        assertEquals(2, item.getOrder());
    }

    @Test
    void testContentItemList() {
        LessonEntity lesson = new LessonEntity();
        LessonEntity.ContentItem item = new LessonEntity.ContentItem("VIDEO", "k", "pk", "READY", "L", 1);
        lesson.setContentKey(List.of(item));

        assertNotNull(lesson.getContentKey());
        assertEquals(1, lesson.getContentKey().size());
        assertEquals("VIDEO", lesson.getContentKey().get(0).getType());
    }
}
