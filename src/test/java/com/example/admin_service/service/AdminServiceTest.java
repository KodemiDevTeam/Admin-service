package com.example.admin_service.service;

import com.example.admin_service.dto.request.*;
import com.example.admin_service.dto.response.*;
import com.example.admin_service.enums.AdminRole;
import com.example.admin_service.enums.Role;
import com.example.admin_service.exceptions.*;
import com.example.admin_service.feign.AuthClient;
import com.example.admin_service.feign.CourseClient;
import com.example.admin_service.feign.PaymentClient;
import com.example.admin_service.feign.UserClient;
import com.example.admin_service.model.Admin;
import com.example.admin_service.repository.AdminRepository;
import com.example.admin_service.util.JwtUtil;
import com.example.admin_service.util.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private AdminService     adminService;
    private UserClient       userClient;
    private AuthClient       authClient;
    private CourseClient     courseClient;
    private PaymentClient    paymentClient;     // ← was missing
    private AdminRepository  adminRepository;
    private JwtUtil          jwtUtil;
    private PasswordEncoder  passwordEncoder;

    private static final String TOKEN = "Bearer token";

    @BeforeEach
    void setup() {
        userClient      = mock(UserClient.class);
        authClient      = mock(AuthClient.class);
        courseClient    = mock(CourseClient.class);
        paymentClient   = mock(PaymentClient.class);   // ← was missing
        adminRepository = mock(AdminRepository.class);
        jwtUtil         = mock(JwtUtil.class);
        passwordEncoder = mock(PasswordEncoder.class);

        adminService = new AdminService(
                userClient, authClient, courseClient,
                paymentClient,                          // ← was missing
                adminRepository, jwtUtil, passwordEncoder
        );
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private Admin buildAdmin(boolean pending) {
        Admin admin = new Admin();
        admin.setAdminId("a1");
        admin.setPassword("hashed");
        admin.setPending(pending);
        admin.setEmail("admin@test.com");
        admin.setUserId("uid_001");
        admin.setAdminRole(AdminRole.USER_ADMIN);
        return admin;
    }

    // ── getUser ──────────────────────────────────────────────────────────

    @Test
    void getUser_trainerRole_returnsTrainer() {
        UserDTO dto = mock(UserDTO.class);
        when(dto.getRole()).thenReturn(Role.TRAINER);
        TrainerResponseDTO response = new TrainerResponseDTO();
        when(authClient.geUserById(TOKEN, "1")).thenReturn(dto);
        when(userClient.getTrainerById(TOKEN, "1")).thenReturn(response);

        assertSame(response, adminService.getUser(TOKEN, "1"));
        verify(userClient).getTrainerById(TOKEN, "1");
    }

    @Test
    void getUser_superAdminRole_returnsAdmin() {
        UserDTO dto = mock(UserDTO.class);
        when(dto.getRole()).thenReturn(Role.SUPER_ADMIN);
        AdminResponseDTO response = new AdminResponseDTO();
        when(authClient.geUserById(TOKEN, "1")).thenReturn(dto);
        when(userClient.getAdmin("1")).thenReturn(response);

        assertSame(response, adminService.getUser(TOKEN, "1"));
        verify(userClient).getAdmin("1");
    }

    @Test
    void getUser_learnerRole_returnsLearner() {
        UserDTO dto = mock(UserDTO.class);
        when(dto.getRole()).thenReturn(Role.LEARNER);
        LearnerResponseDTO response = new LearnerResponseDTO();
        when(authClient.geUserById(TOKEN, "1")).thenReturn(dto);
        when(userClient.getLearner(TOKEN, "1")).thenReturn(response);

        assertSame(response, adminService.getUser(TOKEN, "1"));
        verify(userClient).getLearner(TOKEN, "1");
    }

    // ── Trainer actions ───────────────────────────────────────────────────

    @Test
    void trainerApprove_callsAuthClient() {
        adminService.trainerApprove(TOKEN, "t1");
        verify(authClient).activateTrainer(TOKEN, "t1");
    }

    @Test
    void rejectApprove_callsAuthClient() {
        adminService.rejectApprove(TOKEN, "t1");
        verify(authClient).rejectTrainer(TOKEN, "t1");
    }

    @Test
    void getAllTrainer_returnsListFromUserClient() {
        List<TrainerResponseDTO> list = Collections.emptyList();
        when(userClient.getAllTrainers(TOKEN)).thenReturn(list);
        assertEquals(list, adminService.getAllTrainer(TOKEN));
    }

    @Test
    void getPendingTrainers_returnsListFromUserClient() {
        List<TrainerResponseDTO> list = Collections.emptyList();
        when(userClient.getAllPendingTrainers(TOKEN)).thenReturn(list);
        assertEquals(list, adminService.getPendingTrainers(TOKEN));
    }

    // ── Course actions ────────────────────────────────────────────────────

    @Test
    void verifyCourse_callsCourseClient() {
        adminService.verifyCourse(TOKEN, "c1");
        verify(courseClient).verifyCourse(TOKEN, "c1");
    }

    @Test
    void rejectCourse_callsCourseClient() {
        adminService.rejectCourse(TOKEN, "c1");
        verify(courseClient).rejectCourse(TOKEN, "c1");
    }

    @Test
    void getAllUnVerified_returnsListFromCourseClient() {
        List<CourseResponseDTO> list = Collections.emptyList();
        when(courseClient.getAllUnVerifiedCourses(TOKEN)).thenReturn(list);
        assertEquals(list, adminService.getAllUnVerified(TOKEN));
    }

    // ── login ─────────────────────────────────────────────────────────────

    @Test
    void login_success_notPending_returnsToken() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("a1");
        dto.setPassword("pass");

        Admin admin = buildAdmin(false);
        when(adminRepository.findById("a1")).thenReturn(admin);
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(authClient.adminLogin(any())).thenReturn("JWT_TOKEN");

        assertEquals("JWT_TOKEN", adminService.login(dto));
    }

    @Test
    void login_pendingAdmin_returnsResponseWrapper() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("a1");
        dto.setPassword("pass");

        Admin admin = buildAdmin(true);
        when(adminRepository.findById("a1")).thenReturn(admin);
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(authClient.adminLogin(any())).thenReturn("JWT_TOKEN");

        Object result = adminService.login(dto);

        assertInstanceOf(Response.class, result);
        Response<?> response = (Response<?>) result;
        assertFalse(response.isSuccess());
        assertEquals("Admin Pending.", response.getMessage());
    }

    @Test
    void login_invalidPassword_throwsInvalidCredentialsException() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("a1");
        dto.setPassword("wrong");

        Admin admin = buildAdmin(false);
        when(adminRepository.findById("a1")).thenReturn(admin);
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> adminService.login(dto));
        verify(authClient, never()).adminLogin(any());
    }

    @Test
    void login_adminNotFound_throwsAdminNotFoundException() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("a1");
        when(adminRepository.findById("a1")).thenReturn(null);

        assertThrows(AdminNotFoundException.class,
                () -> adminService.login(dto));
    }

    // ── subAdminCreate ────────────────────────────────────────────────────

    @Test
    void subAdminCreate_success_savesAndReturnsMessage() {
        try (var mocked = mockStatic(PasswordValidator.class)) {
            mocked.when(() -> PasswordValidator.validate(any())).thenAnswer(i -> null);

            SubAdminRequest request = new SubAdminRequest();
            request.setAdminId("sub1");
            request.setPassword("Password123!");
            request.setAdminRole(AdminRole.USER_ADMIN);

            when(jwtUtil.extractEmail(TOKEN)).thenReturn("admin@test.com");
            when(jwtUtil.extractUserId(TOKEN)).thenReturn("uid_001");

            String result = adminService.subAdminCreate(TOKEN, request);

            assertTrue(result.contains("Sub Admin Created"));
            assertTrue(result.contains("USER_ADMIN"));
            verify(adminRepository).save(any(Admin.class));
        }
    }

    @Test
    void subAdminCreate_invalidPassword_throwsException() {
        try (var mocked = mockStatic(PasswordValidator.class)) {
            mocked.when(() -> PasswordValidator.validate("weak"))
                    .thenThrow(new IllegalArgumentException("Weak password"));

            SubAdminRequest request = new SubAdminRequest();
            request.setAdminId("sub1");
            request.setPassword("weak");
            request.setAdminRole(AdminRole.USER_ADMIN);

            when(jwtUtil.extractEmail(TOKEN)).thenReturn("admin@test.com");
            when(jwtUtil.extractUserId(TOKEN)).thenReturn("uid_001");

            assertThrows(IllegalArgumentException.class,
                    () -> adminService.subAdminCreate(TOKEN, request));
            verify(adminRepository, never()).save(any());
        }
    }

    @Test
    void subAdminCreate_setsAdminFieldsCorrectly() {
        try (var mocked = mockStatic(PasswordValidator.class)) {
            mocked.when(() -> PasswordValidator.validate(any())).thenAnswer(i -> null);

            SubAdminRequest request = new SubAdminRequest();
            request.setAdminId("sub2");
            request.setPassword("Password123!");
            request.setAdminRole(AdminRole.COURSE_ADMIN);

            when(jwtUtil.extractEmail(TOKEN)).thenReturn("super@test.com");
            when(jwtUtil.extractUserId(TOKEN)).thenReturn("uid_super");

            adminService.subAdminCreate(TOKEN, request);

            verify(adminRepository).save(argThat(admin ->
                    admin.getAdminId().equals("sub2") &&
                            admin.getAdminRole() == AdminRole.COURSE_ADMIN &&
                            admin.isPending() &&
                            admin.getEmail().equals("super@test.com") &&
                            admin.getUserId().equals("uid_super")
            ));
        }
    }

    // ── setSubAdmin ───────────────────────────────────────────────────────

    @Test
    void setSubAdmin_pendingAdmin_updatesAndReturnsSuccess() {
        try (var mocked = mockStatic(PasswordValidator.class)) {
            mocked.when(() -> PasswordValidator.validate(any())).thenAnswer(i -> null);

            SubAdminDetailsDTO dto = new SubAdminDetailsDTO();
            dto.setUsername("newUser");
            dto.setPassword("Password123!");

            Admin admin = buildAdmin(true);
            when(jwtUtil.extractRole(TOKEN)).thenReturn("USER_ADMIN");
            when(adminRepository.findByRole(AdminRole.USER_ADMIN)).thenReturn(admin);

            String result = adminService.setSubAdmin(dto, TOKEN);

            assertEquals("Username Set.", result);
            assertFalse(admin.isPending());
            assertEquals("newUser", admin.getUsername());
            verify(adminRepository).save(admin);
        }
    }

    @Test
    void setSubAdmin_alreadyUpdated_returnsAlreadyUpdatedMessage() {
        SubAdminDetailsDTO dto = new SubAdminDetailsDTO();
        Admin admin = buildAdmin(false);   // not pending

        when(jwtUtil.extractRole(TOKEN)).thenReturn("USER_ADMIN");
        when(adminRepository.findByRole(AdminRole.USER_ADMIN)).thenReturn(admin);

        String result = adminService.setSubAdmin(dto, TOKEN);

        assertEquals("SubAdmin Already Updated.", result);
        verify(adminRepository, never()).save(any());
    }

    @Test
    void setSubAdmin_invalidPassword_throwsException() {
        try (var mocked = mockStatic(PasswordValidator.class)) {
            mocked.when(() -> PasswordValidator.validate("weak"))
                    .thenThrow(new IllegalArgumentException("Weak password"));

            SubAdminDetailsDTO dto = new SubAdminDetailsDTO();
            dto.setUsername("newUser");
            dto.setPassword("weak");

            Admin admin = buildAdmin(true);
            when(jwtUtil.extractRole(TOKEN)).thenReturn("USER_ADMIN");
            when(adminRepository.findByRole(AdminRole.USER_ADMIN)).thenReturn(admin);

            assertThrows(IllegalArgumentException.class,
                    () -> adminService.setSubAdmin(dto, TOKEN));
            verify(adminRepository, never()).save(any());
        }
    }

    // ── changePassword ────────────────────────────────────────────────────

    @Test
    void changePassword_success_updatesPassword() {
        try (var mocked = mockStatic(PasswordValidator.class)) {
            mocked.when(() -> PasswordValidator.validate(any())).thenAnswer(i -> null);

            Admin admin = buildAdmin(false);
            when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
            when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(admin);

            String result = adminService.changePassword("Password123!", TOKEN);

            assertEquals("Password Changed Successfully.", result);
            verify(adminRepository).save(admin);
        }
    }

    @Test
    void changePassword_adminNotFound_throwsAdminNotFoundException() {
        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(null);

        assertThrows(AdminNotFoundException.class,
                () -> adminService.changePassword("Password123!", TOKEN));
        verify(adminRepository, never()).save(any());
    }

    @Test
    void changePassword_invalidPassword_throwsException() {
        try (var mocked = mockStatic(PasswordValidator.class)) {
            mocked.when(() -> PasswordValidator.validate("weak"))
                    .thenThrow(new IllegalArgumentException("Weak password"));

            Admin admin = buildAdmin(false);
            when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
            when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(admin);

            assertThrows(IllegalArgumentException.class,
                    () -> adminService.changePassword("weak", TOKEN));
            verify(adminRepository, never()).save(any());
        }
    }

    // ── getPendingPayout ──────────────────────────────────────────────────

    @Test
    void getPendingPayout_success_returnsListFromPaymentClient() {
        List<PayoutRequest> list = List.of(new PayoutRequest());
        when(paymentClient.getPendingPayouts()).thenReturn(list);

        List<PayoutRequest> result = adminService.getPendingPayout();

        assertEquals(list, result);
        verify(paymentClient).getPendingPayouts();
    }

    @Test
    void getPendingPayout_emptyList_returnsEmpty() {
        when(paymentClient.getPendingPayouts()).thenReturn(Collections.emptyList());

        List<PayoutRequest> result = adminService.getPendingPayout();

        assertTrue(result.isEmpty());
    }

    @Test
    void getPendingPayout_clientThrows_throwsFetchPendingPayoutException() {
        when(paymentClient.getPendingPayouts())
                .thenThrow(new RuntimeException("Feign error"));

        assertThrows(FetchPendingPayoutException.class,
                () -> adminService.getPendingPayout());
    }

    // ── processPayoutRequest ──────────────────────────────────────────────

    @Test
    void processPayoutRequest_success_returnsResultFromPaymentClient() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();
        when(paymentClient.processPayoutRequest(request, TOKEN)).thenReturn("Payout Processed");

        String result = adminService.processPayoutRequest(TOKEN, request);

        assertEquals("Payout Processed", result);
        verify(paymentClient).processPayoutRequest(request, TOKEN);
    }

    @Test
    void processPayoutRequest_nullToken_throwsUnauthorizedPayoutAccessException() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();

        assertThrows(UnauthorizedPayoutAccessException.class,
                () -> adminService.processPayoutRequest(null, request));
        verify(paymentClient, never()).processPayoutRequest(any(), any());
    }

    @Test
    void processPayoutRequest_blankToken_throwsUnauthorizedPayoutAccessException() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();

        assertThrows(UnauthorizedPayoutAccessException.class,
                () -> adminService.processPayoutRequest("   ", request));
        verify(paymentClient, never()).processPayoutRequest(any(), any());
    }

    @Test
    void processPayoutRequest_clientThrows_throwsPayoutProcessingException() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();
        when(paymentClient.processPayoutRequest(any(), any()))
                .thenThrow(new RuntimeException("Feign error"));

        assertThrows(PayoutProcessingException.class,
                () -> adminService.processPayoutRequest(TOKEN, request));
    }

    // ── processPayoutRequestByPath ────────────────────────────────────────

    @Test
    void processPayoutRequestByPath_approve_success() {
        when(paymentClient.processPayoutRequestByPath(TOKEN, "APPROVE", "payout1", "Approved"))
                .thenReturn("Approved successfully");

        String result = adminService.processPayoutRequestByPath(
                TOKEN, "APPROVE", "payout1", "Approved");

        assertEquals("Approved successfully", result);
        verify(paymentClient).processPayoutRequestByPath(TOKEN, "APPROVE", "payout1", "Approved");
    }

    @Test
    void processPayoutRequestByPath_reject_success() {
        when(paymentClient.processPayoutRequestByPath(TOKEN, "REJECT", "payout1", "Insufficient docs"))
                .thenReturn("Rejected successfully");

        String result = adminService.processPayoutRequestByPath(
                TOKEN, "REJECT", "payout1", "Insufficient docs");

        assertEquals("Rejected successfully", result);
    }

    @Test
    void processPayoutRequestByPath_caseInsensitive_approve() {
        when(paymentClient.processPayoutRequestByPath(any(), any(), any(), any()))
                .thenReturn("Approved");

        // lowercase should also work — equalsIgnoreCase
        assertDoesNotThrow(() ->
                adminService.processPayoutRequestByPath(TOKEN, "approve", "p1", "ok"));
    }

    @Test
    void processPayoutRequestByPath_invalidAction_throwsInvalidPayoutActionException() {
        assertThrows(InvalidPayoutActionException.class, () ->
                adminService.processPayoutRequestByPath(
                        TOKEN, "HOLD", "payout1", "remarks"));
        verify(paymentClient, never())
                .processPayoutRequestByPath(any(), any(), any(), any());
    }

    @Test
    void processPayoutRequestByPath_randomAction_throwsInvalidPayoutActionException() {
        assertThrows(InvalidPayoutActionException.class, () ->
                adminService.processPayoutRequestByPath(
                        TOKEN, "CANCEL", "payout1", "remarks"));
    }

    @Test
    void processPayoutRequestByPath_clientThrows_throwsPaymentClientException() {
        when(paymentClient.processPayoutRequestByPath(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Feign error"));

        assertThrows(PaymentClientException.class, () ->
                adminService.processPayoutRequestByPath(
                        TOKEN, "APPROVE", "payout1", "ok"));
    }
}