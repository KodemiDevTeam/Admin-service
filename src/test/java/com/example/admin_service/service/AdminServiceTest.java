package com.example.admin_service.service;

import com.example.admin_service.dto.request.*;
import com.example.admin_service.dto.response.*;
import com.example.admin_service.enums.AdminRole;
import com.example.admin_service.enums.Role;
import com.example.admin_service.exceptions.*;
import com.example.admin_service.feign.*;
import com.example.admin_service.model.Admin;
import com.example.admin_service.model.AdminRateLimit;
import com.example.admin_service.repository.AdminRateLimitRepository;
import com.example.admin_service.repository.AdminRepository;
import com.example.admin_service.util.JwtUtil;
import com.example.admin_service.service.notification.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private static final String TOKEN = "Bearer token";
    private static final String HASHED_PASSWORD = "hashed";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String APPROVE = "APPROVE";

    private AdminService             adminService;
    private UserClient               userClient;
    private AuthClient               authClient;
    private CourseClient             courseClient;
    private PaymentClient            paymentClient;
    private AdminRepository adminRepository;
    private JwtUtil                  jwtUtil;
    private PasswordEncoder          passwordEncoder;
    private EmailService             emailService;
    private AdminRateLimitRepository adminRateLimitRepository;

    @BeforeEach
    void setup() {
        userClient               = mock(UserClient.class);
        authClient               = mock(AuthClient.class);
        courseClient             = mock(CourseClient.class);
        paymentClient            = mock(PaymentClient.class);
        adminRepository          = mock(AdminRepository.class);
        jwtUtil                  = mock(JwtUtil.class);
        passwordEncoder          = mock(PasswordEncoder.class);
        SecureRandom secureRandom = mock(SecureRandom.class);
        emailService             = mock(EmailService.class);
        NotificationPublisher notificationPublisher = mock(NotificationPublisher.class);
        adminRateLimitRepository = mock(AdminRateLimitRepository.class);

        // Mock secureRandom behaviour so generatePassword() doesn't throw null pointer exceptions
        when(secureRandom.nextInt(anyInt())).thenReturn(0);

        adminService = new AdminService(
                userClient, authClient, courseClient, paymentClient,
                adminRepository, jwtUtil, passwordEncoder, secureRandom,
                emailService, notificationPublisher, adminRateLimitRepository
        );
    }

    private Admin buildAdmin(boolean pending) {
        Admin admin = new Admin();
        admin.setAdminId("a1");
        admin.setPassword(HASHED_PASSWORD);
        admin.setPending(pending);
        admin.setEmail(ADMIN_EMAIL);
        admin.setAdminRole(AdminRole.USER_ADMIN);
        return admin;
    }

    // ── getUser ──────────────────────────────────────────────────────────







    // ── Trainer actions ───────────────────────────────────────────────────

    @Test
    void trainerModerationValidIdCallsAuthClient() {
        Object response = new Object();
        when(authClient.reviewTrainer(eq(TOKEN), eq("t1"), any(TrainerReviewRequest.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok(response));

        Object result = adminService.trainerModeration(TOKEN, "t1", APPROVE, "Ok");
        assertNotNull(result);
        verify(authClient).reviewTrainer(eq(TOKEN), eq("t1"), any(TrainerReviewRequest.class));
    }

    @Test
    void trainerModerationNullIdReturnsPendingTrainers() {
        List<TrainerResponseDTO> pending = new ArrayList<>();
        when(userClient.getAllPendingTrainers(TOKEN)).thenReturn(pending);

        Object result = adminService.trainerModeration(TOKEN, null, APPROVE, "Ok");
        assertTrue(result instanceof Map);
        Map<?, ?> map = (Map<?, ?>) result;
        assertEquals(pending, map.get("data"));
    }

    @Test
    void getAllTrainerReturnsListFromUserClient() {
        List<TrainerResponseDTO> list = Collections.emptyList();
        when(userClient.getAllTrainers(TOKEN)).thenReturn(list);
        assertEquals(list, adminService.getAllTrainer(TOKEN));
    }

    // ── Course actions ────────────────────────────────────────────────────

    @Test
    void courseModerationValidIdCallsCourseClient() {
        Map<String, Object> mockRes = new HashMap<>();
        when(courseClient.reviewCourse(eq(TOKEN), eq("c1"), any(CourseModerationRequest.class)))
                .thenReturn(mockRes);

        Map<String, Object> result = adminService.courseModeration(TOKEN, "c1", APPROVE, "Good");
        assertEquals(mockRes, result);
        verify(courseClient).reviewCourse(eq(TOKEN), eq("c1"), any(CourseModerationRequest.class));
    }





    @Test
    void getAllPayoutsSuccess() {
        List<PayoutRequest> list = List.of(new PayoutRequest());
        when(paymentClient.getAllPayouts(TOKEN)).thenReturn(list);

        List<PayoutRequest> result = adminService.getAllPayouts(TOKEN);
        assertEquals(list, result);
    }

    @Test
    void getAllPayoutsFailsThrowsFetchPendingPayoutException() {
        when(paymentClient.getAllPayouts(TOKEN)).thenThrow(new RuntimeException("Error"));
        assertThrows(FetchPendingPayoutException.class, () -> adminService.getAllPayouts(TOKEN));
    }

    @Test
    void getAllTransactionHistorySuccess() {
        TransactionHistoryResponse response = new TransactionHistoryResponse();
        when(paymentClient.getTransactionHistory()).thenReturn(response);

        assertEquals(response, adminService.getAllTransactionHistory());
    }

    @Test
    void getAllTransactionHistoryFailsThrowsFetchPendingPayoutException() {
        when(paymentClient.getTransactionHistory()).thenThrow(new RuntimeException("Error"));
        assertThrows(FetchPendingPayoutException.class, () -> adminService.getAllTransactionHistory());
    }

    @Test
    void processPayoutRequestSuccess() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();
        when(paymentClient.processPayoutRequest(request, TOKEN)).thenReturn("Success");

        assertEquals("Success", adminService.processPayoutRequest(TOKEN, request));
    }

    @Test
    void processPayoutRequestNullTokenThrows() {
        assertThrows(UnauthorizedPayoutAccessException.class, () -> adminService.processPayoutRequest(null, new ProcessPayoutRequest()));
    }

    @Test
    void processPayoutRequestByPathSuccess() {
        when(paymentClient.processPayoutRequestByPath(TOKEN, APPROVE, "p1", "ok")).thenReturn("Success");
        assertEquals("Success", adminService.processPayoutRequestByPath(TOKEN, APPROVE, "p1", "ok"));
    }

    @Test
    void processPayoutRequestByPathInvalidActionThrows() {
        assertThrows(InvalidPayoutActionException.class, () -> adminService.processPayoutRequestByPath(TOKEN, "CANCEL", "p1", "ok"));
    }

    // ── announcements & suspension ─────────────────────────────────────────



    @Test
    void broadcastAnnouncementRateLimitExceededThrows() {
        AdminBroadcastRequest request = new AdminBroadcastRequest();
        AdminRateLimit rateLimit = new AdminRateLimit();
        rateLimit.setBroadcastCount(5);

        when(jwtUtil.extractUserId(TOKEN)).thenReturn("admin-1");
        when(adminRateLimitRepository.getAdminRateLimit(anyString())).thenReturn(rateLimit);

        assertThrows(RuntimeException.class, () -> adminService.broadcastAnnouncement(TOKEN, request));
    }


}