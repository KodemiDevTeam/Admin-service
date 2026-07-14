package com.example.admin_service.service;

import com.example.admin_service.dto.notification.BroadcastNotificationRequest;
import com.example.admin_service.dto.notification.NotificationRequest;
import com.example.admin_service.dto.request.*;
import com.example.admin_service.dto.response.TrainerResponseDTO;
import com.example.admin_service.enums.AdminRole;
import com.example.admin_service.enums.Role;
import com.example.admin_service.exceptions.*;
import com.example.admin_service.feign.*;
import com.example.admin_service.model.Admin;
import com.example.admin_service.model.AdminRateLimit;
import com.example.admin_service.repository.AdminRateLimitRepository;
import com.example.admin_service.repository.AdminRepository;
import com.example.admin_service.service.notification.NotificationPublisher;
import com.example.admin_service.util.JwtUtil;
import com.example.admin_service.util.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceExtraTest {

    private AdminService             adminService;
    private UserClient               userClient;
    private AuthClient               authClient;
    private CourseClient             courseClient;
    private PaymentClient            paymentClient;
    private AdminRepository          adminRepository;
    private JwtUtil                  jwtUtil;
    private PasswordEncoder          passwordEncoder;
    private SecureRandom             secureRandom;
    private EmailService             emailService;
    private NotificationPublisher    notificationPublisher;
    private AdminRateLimitRepository adminRateLimitRepository;

    private static final String TOKEN = "Bearer token";

    @BeforeEach
    void setup() {
        userClient               = mock(UserClient.class);
        authClient               = mock(AuthClient.class);
        courseClient             = mock(CourseClient.class);
        paymentClient            = mock(PaymentClient.class);
        adminRepository          = mock(AdminRepository.class);
        jwtUtil                  = mock(JwtUtil.class);
        passwordEncoder          = mock(PasswordEncoder.class);
        secureRandom             = mock(SecureRandom.class);
        emailService             = mock(EmailService.class);
        notificationPublisher    = mock(NotificationPublisher.class);
        adminRateLimitRepository = mock(AdminRateLimitRepository.class);

        when(secureRandom.nextInt(anyInt())).thenReturn(0);

        adminService = new AdminService(
                userClient, authClient, courseClient, paymentClient,
                adminRepository, jwtUtil, passwordEncoder, secureRandom,
                emailService, notificationPublisher, adminRateLimitRepository
        );
    }

    // ── trainerModeration REJECT branch ───────────────────────────────────

    @Test
    void trainerModeration_rejectAction_publishesRejectionNotification() {
        when(authClient.reviewTrainer(eq(TOKEN), eq("t1"), any(TrainerReviewRequest.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("ok"));

        Object result = adminService.trainerModeration(TOKEN, "t1", "REJECT", "Not qualified");

        assertNotNull(result);
        verify(notificationPublisher).publish(any(NotificationRequest.class));
    }

    @Test
    void trainerModeration_otherAction_noNotification() {
        when(authClient.reviewTrainer(eq(TOKEN), eq("t1"), any(TrainerReviewRequest.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("ok"));

        Object result = adminService.trainerModeration(TOKEN, "t1", "HOLD", "On hold");

        assertNotNull(result);
        verify(notificationPublisher, never()).publish(any());
    }

    @Test
    void trainerModeration_approveAction_publishesApprovalNotification() {
        when(authClient.reviewTrainer(eq(TOKEN), eq("t1"), any(TrainerReviewRequest.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("ok"));

        adminService.trainerModeration(TOKEN, "t1", "APPROVE", "Good");

        verify(notificationPublisher).publish(any(NotificationRequest.class));
    }

    @Test
    void trainerModeration_notificationFails_doesNotThrow() {
        when(authClient.reviewTrainer(eq(TOKEN), eq("t1"), any(TrainerReviewRequest.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("ok"));
        doThrow(new RuntimeException("kafka down")).when(notificationPublisher).publish(any());

        assertDoesNotThrow(() -> adminService.trainerModeration(TOKEN, "t1", "APPROVE", "Ok"));
    }

    // ── changePassword admin not found ─────────────────────────────────────

    @Test
    void changePassword_adminNotFound_throwsAdminNotFoundException() {
        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(null);

        assertThrows(AdminNotFoundException.class,
                () -> adminService.changePassword("Password123!", TOKEN));
    }

    // ── processPayoutRequest failure path ─────────────────────────────────

    @Test
    void processPayoutRequest_paymentClientThrows_throwsPayoutProcessingException() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();
        when(paymentClient.processPayoutRequest(request, TOKEN)).thenThrow(new RuntimeException("timeout"));

        assertThrows(PayoutProcessingException.class,
                () -> adminService.processPayoutRequest(TOKEN, request));
    }

    @Test
    void processPayoutRequest_blankToken_throwsUnauthorized() {
        assertThrows(UnauthorizedPayoutAccessException.class,
                () -> adminService.processPayoutRequest("   ", new ProcessPayoutRequest()));
    }

    // ── processPayoutRequestByPath failure path ────────────────────────────

    @Test
    void processPayoutRequestByPath_paymentClientThrows_throwsPaymentClientException() {
        when(paymentClient.processPayoutRequestByPath(TOKEN, "REJECT", "p1", null))
                .thenThrow(new RuntimeException("service unavailable"));

        assertThrows(PaymentClientException.class,
                () -> adminService.processPayoutRequestByPath(TOKEN, "REJECT", "p1", null));
    }

    @Test
    void processPayoutRequestByPath_holdAction_success() {
        when(paymentClient.processPayoutRequestByPath(TOKEN, "HOLD", "p1", "hold"))
                .thenReturn("on hold");

        assertEquals("on hold",
                adminService.processPayoutRequestByPath(TOKEN, "HOLD", "p1", "hold"));
    }

    // ── suspendUser failure path ───────────────────────────────────────────

    @Test
    void suspendUser_publisherThrows_throwsUserSuspendException() {
        doThrow(new RuntimeException("notification down")).when(notificationPublisher).publish(any());

        assertThrows(UserSuspendException.class,
                () -> adminService.suspendUser("user-1", "Spam"));
    }

    // ── broadcastAnnouncement non-urgent removes SMS ───────────────────────

    @Test
    void broadcastAnnouncement_notUrgent_removesSMS() {
        AdminBroadcastRequest request = new AdminBroadcastRequest();
        request.setTitle("Test");
        request.setMessage("Msg");
        request.setTargetRole("ALL");
        request.setUrgent(false);
        request.setChannels(new ArrayList<>(List.of("EMAIL", "SMS")));

        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("admin-1");
        when(adminRateLimitRepository.getAdminRateLimit(anyString())).thenReturn(null);

        String result = adminService.broadcastAnnouncement(TOKEN, request);

        assertEquals("Broadcast sent successfully", result);
        verify(notificationPublisher).publishBroadcast(any(BroadcastNotificationRequest.class));
    }

    @Test
    void broadcastAnnouncement_smsNotSuperAdmin_throwsBroadcastException() {
        AdminBroadcastRequest request = new AdminBroadcastRequest();
        request.setTitle("Test");
        request.setMessage("Msg");
        request.setTargetRole("ALL");
        request.setUrgent(true);
        request.setChannels(new ArrayList<>(List.of("EMAIL", "SMS")));

        when(jwtUtil.extractRole(TOKEN)).thenReturn("PAYMENT_ADMIN");
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("admin-1");
        when(adminRateLimitRepository.getAdminRateLimit(anyString())).thenReturn(null);

        assertThrows(BroadcastException.class,
                () -> adminService.broadcastAnnouncement(TOKEN, request));
    }

    @Test
    void broadcastAnnouncement_existingRateLimit_incrementsCount() {
        AdminBroadcastRequest request = new AdminBroadcastRequest();
        request.setTitle("Test");
        request.setMessage("Msg");
        request.setTargetRole("ALL");
        request.setUrgent(false);
        request.setChannels(new ArrayList<>(List.of("EMAIL")));

        AdminRateLimit rateLimit = new AdminRateLimit();
        rateLimit.setBroadcastCount(2);

        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("admin-1");
        when(adminRateLimitRepository.getAdminRateLimit(anyString())).thenReturn(rateLimit);

        String result = adminService.broadcastAnnouncement(TOKEN, request);

        assertEquals("Broadcast sent successfully", result);
        assertEquals(3, rateLimit.getBroadcastCount());
    }

    @Test
    void broadcastAnnouncement_withSpecificTargetRole_usesRoleBasedMode() {
        AdminBroadcastRequest request = new AdminBroadcastRequest();
        request.setTitle("Test");
        request.setMessage("Msg");
        request.setTargetRole("TRAINER");
        request.setUrgent(false);
        request.setChannels(new ArrayList<>(List.of("EMAIL")));

        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("admin-1");
        when(adminRateLimitRepository.getAdminRateLimit(anyString())).thenReturn(null);

        String result = adminService.broadcastAnnouncement(TOKEN, request);

        assertEquals("Broadcast sent successfully", result);
        verify(notificationPublisher).publishBroadcast(any(BroadcastNotificationRequest.class));
    }

    @Test
    void broadcastAnnouncement_publisherThrows_throwsBroadcastException() {
        AdminBroadcastRequest request = new AdminBroadcastRequest();
        request.setTitle("Test");
        request.setMessage("Msg");
        request.setTargetRole("ALL");
        request.setUrgent(false);
        request.setChannels(new ArrayList<>(List.of("EMAIL")));

        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("admin-1");
        when(adminRateLimitRepository.getAdminRateLimit(anyString())).thenReturn(null);
        doThrow(new RuntimeException("kafka down")).when(notificationPublisher).publishBroadcast(any());

        assertThrows(BroadcastException.class,
                () -> adminService.broadcastAnnouncement(TOKEN, request));
    }

    // ── generatePassword coverage ──────────────────────────────────────────

    @Test
    void generatePassword_returnsEightCharPassword() {
        // Use real SecureRandom for this test
        SecureRandom realRandom = new SecureRandom();
        AdminService svc = new AdminService(
                userClient, authClient, courseClient, paymentClient,
                adminRepository, jwtUtil, passwordEncoder, realRandom,
                emailService, notificationPublisher, adminRateLimitRepository
        );
        String pwd = svc.generatePassword();
        assertNotNull(pwd);
        assertEquals(8, pwd.length());
    }

    // ── courseModeration allCourses null ────────────────────────────────────

    @Test
    void courseModeration_nullCoursesList_returnsEmptyData() {
        when(courseClient.getAllCoursesAdmin(TOKEN)).thenReturn(null);

        Map<String, Object> result = adminService.courseModeration(TOKEN, null, null, null);
        assertNotNull(result);
        List<?> data = (List<?>) result.get("data");
        assertTrue(data.isEmpty());
    }
}
