package com.example.admin_service.controller;

import com.example.admin_service.dto.request.CourseModerationRequest;
import com.example.admin_service.dto.request.TrainerReviewRequest;
import com.example.admin_service.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerExtraTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private final String TOKEN = "Bearer token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ── courseReviewCompat: null request uses query params ─────────────────

    @Test
    void courseReviewCompat_nullRequest_usesQueryParams() {
        Map<String, Object> mockRes = new HashMap<>();
        when(adminService.courseModeration(TOKEN, "c1", "REJECT", "not good")).thenReturn(mockRes);

        ResponseEntity<Map<String, Object>> response =
                adminController.courseReviewCompat(TOKEN, "c1", null, "REJECT", "not good");

        assertEquals(mockRes, response.getBody());
        verify(adminService).courseModeration(TOKEN, "c1", "REJECT", "not good");
    }

    @Test
    void courseReviewCompat_nullActionFallsBackToApprove() {
        Map<String, Object> mockRes = new HashMap<>();
        when(adminService.courseModeration(TOKEN, "c1", "APPROVE", null)).thenReturn(mockRes);

        ResponseEntity<Map<String, Object>> response =
                adminController.courseReviewCompat(TOKEN, "c1", null, null, null);

        assertEquals(mockRes, response.getBody());
        verify(adminService).courseModeration(TOKEN, "c1", "APPROVE", null);
    }

    @Test
    void courseReviewCompat_requestActionOverriddenByQueryParam() {
        Map<String, Object> mockRes = new HashMap<>();
        CourseModerationRequest request = new CourseModerationRequest();
        request.setAction("APPROVE");
        request.setRemarks("from body");

        when(adminService.courseModeration(TOKEN, "c1", "REJECT", "from body")).thenReturn(mockRes);

        ResponseEntity<Map<String, Object>> response =
                adminController.courseReviewCompat(TOKEN, "c1", request, "REJECT", null);

        assertEquals(mockRes, response.getBody());
    }

    // ── trainerReviewCompat: null request uses query params ────────────────

    @Test
    void trainerReviewCompat_nullRequest_usesQueryParams() {
        Object mockRes = new Object();
        when(adminService.trainerModeration(TOKEN, "t1", "REJECT", "not qualified")).thenReturn(mockRes);

        ResponseEntity<Object> response =
                adminController.trainerReviewCompat(TOKEN, "t1", null, "REJECT", "not qualified");

        assertEquals(mockRes, response.getBody());
        verify(adminService).trainerModeration(TOKEN, "t1", "REJECT", "not qualified");
    }

    @Test
    void trainerReviewCompat_nullActionFallsBackToApprove() {
        Object mockRes = new Object();
        when(adminService.trainerModeration(TOKEN, "t1", "APPROVE", null)).thenReturn(mockRes);

        ResponseEntity<Object> response =
                adminController.trainerReviewCompat(TOKEN, "t1", null, null, null);

        assertEquals(mockRes, response.getBody());
        verify(adminService).trainerModeration(TOKEN, "t1", "APPROVE", null);
    }

    @Test
    void trainerReviewCompat_withRequest_usesRequestFields() {
        Object mockRes = new Object();
        TrainerReviewRequest request = new TrainerReviewRequest();
        request.setAction("APPROVE");
        request.setRemarks("looks good");

        when(adminService.trainerModeration(TOKEN, "t1", "APPROVE", "looks good")).thenReturn(mockRes);

        ResponseEntity<Object> response =
                adminController.trainerReviewCompat(TOKEN, "t1", request, null, null);

        assertEquals(mockRes, response.getBody());
    }
}
