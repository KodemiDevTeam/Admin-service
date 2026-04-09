package com.example.admin_service.service;

import com.example.admin_service.dto.request.*;
import com.example.admin_service.dto.response.*;
import com.example.admin_service.enums.AdminRole;
import com.example.admin_service.enums.Role;
import com.example.admin_service.exceptions.AdminNotFoundException;
import com.example.admin_service.exceptions.InvalidCredentialsException;
import com.example.admin_service.feign.AuthClient;
import com.example.admin_service.feign.CourseClient;
import com.example.admin_service.feign.UserClient;
import com.example.admin_service.model.Admin;
import com.example.admin_service.repository.AdminRepository;
import com.example.admin_service.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    private AdminService adminService;
    private UserClient userClient;
    private AuthClient authClient;
    private CourseClient courseClient;
    private AdminRepository adminRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;

    private static final String TOKEN = "Bearer token";

    @BeforeEach
    void setup() {
        userClient = mock(UserClient.class);
        authClient = mock(AuthClient.class);
        courseClient = mock(CourseClient.class);
        adminRepository = mock(AdminRepository.class);
        jwtUtil = mock(JwtUtil.class);
        passwordEncoder = mock(PasswordEncoder.class);

        adminService = new AdminService(
                userClient, authClient, courseClient,
                adminRepository, jwtUtil, passwordEncoder
        );
    }

    // ================= getUser =================

    @Test
    void getUser_trainerRole() {
        UserDTO dto = mock(UserDTO.class);
        when(dto.getRole()).thenReturn(Role.TRAINER);

        TrainerResponseDTO response = new TrainerResponseDTO();

        when(authClient.geUserById(TOKEN, "1")).thenReturn(dto);
        when(userClient.getTrainerById(TOKEN, "1")).thenReturn(response);

        assertSame(response, adminService.getUser(TOKEN, "1"));
    }

    @Test
    void getUser_superAdminRole() {
        UserDTO dto = mock(UserDTO.class);
        when(dto.getRole()).thenReturn(Role.SUPER_ADMIN);

        AdminResponseDTO response = new AdminResponseDTO();

        when(authClient.geUserById(TOKEN, "1")).thenReturn(dto);
        when(userClient.getAdmin("1")).thenReturn(response);

        assertSame(response, adminService.getUser(TOKEN, "1"));
    }

    @Test
    void getUser_learnerRole() {
        UserDTO dto = mock(UserDTO.class);
        when(dto.getRole()).thenReturn(Role.LEARNER);

        LearnerResponseDTO response = new LearnerResponseDTO();

        when(authClient.geUserById(TOKEN, "1")).thenReturn(dto);
        when(userClient.getLearner(TOKEN, "1")).thenReturn(response);

        assertSame(response, adminService.getUser(TOKEN, "1"));
    }

    // ================= Trainer Actions =================

    @Test
    void trainerApprove() {
        adminService.trainerApprove(TOKEN, "t1");
        verify(authClient).activateTrainer(TOKEN, "t1");
    }

    @Test
    void rejectApprove() {
        adminService.rejectApprove(TOKEN, "t1");
        verify(authClient).rejectTrainer(TOKEN, "t1");
    }

    // ================= Course =================

    @Test
    void verifyCourse() {
        adminService.verifyCourse(TOKEN, "c1");
        verify(courseClient).verifyCourse(TOKEN, "c1");
    }

    @Test
    void rejectCourse() {
        adminService.rejectCourse(TOKEN, "c1");
        verify(courseClient).rejectCourse(TOKEN, "c1");
    }

    @Test
    void getAllUnVerified() {
        List<CourseResponseDTO> list = Collections.emptyList();
        when(courseClient.getAllUnVerifiedCourses(TOKEN)).thenReturn(list);

        assertEquals(list, adminService.getAllUnVerified(TOKEN));
    }

    // ================= Trainer Fetch =================

    @Test
    void getAllTrainer() {
        List<TrainerResponseDTO> list = Collections.emptyList();
        when(userClient.getAllTrainers(TOKEN)).thenReturn(list);

        assertEquals(list, adminService.getAllTrainer(TOKEN));
    }

    @Test
    void getPendingTrainers() {
        List<TrainerResponseDTO> list = Collections.emptyList();
        when(userClient.getAllPendingTrainers(TOKEN)).thenReturn(list);

        assertEquals(list, adminService.getPendingTrainers(TOKEN));
    }

    // ================= login =================

    @Test
    void login_success() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("a1");
        dto.setPassword("pass");

        Admin admin = new Admin();
        admin.setPassword("hashed");
        admin.setPending(false);

        when(adminRepository.findById("a1")).thenReturn(admin);
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(authClient.adminLogin(any())).thenReturn("TOKEN");

        assertEquals("TOKEN", adminService.login(dto));
    }

    @Test
    void login_pendingAdmin() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("a1");
        dto.setPassword("pass");

        Admin admin = new Admin();
        admin.setPassword("hashed");
        admin.setPending(true);

        when(adminRepository.findById("a1")).thenReturn(admin);
        when(passwordEncoder.matches("pass", "hashed")).thenReturn(true);
        when(authClient.adminLogin(any())).thenReturn("TOKEN");

        Object result = adminService.login(dto);

        assertTrue(result instanceof Response);
    }

    @Test
    void login_invalidPassword() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("a1");
        dto.setPassword("wrong");

        Admin admin = new Admin();
        admin.setPassword("hashed");

        when(adminRepository.findById("a1")).thenReturn(admin);
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> adminService.login(dto));
    }

    @Test
    void login_adminNotFound() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("a1");

        when(adminRepository.findById("a1")).thenReturn(null);

        assertThrows(AdminNotFoundException.class,
                () -> adminService.login(dto));
    }

    // ================= subAdminCreate =================

    @Test
    void subAdminCreate_success() {
        SubAdminRequest request = new SubAdminRequest();
        request.setAdminId("sub1");
        request.setPassword("Password123!");
        request.setAdminRole(AdminRole.USER_ADMIN);

        when(jwtUtil.extractEmail(TOKEN)).thenReturn("mail@test.com");
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("uid");

        String result = adminService.subAdminCreate(TOKEN, request);

        assertTrue(result.contains("Sub Admin Created"));
        verify(adminRepository).save(any(Admin.class));
    }

    // ================= setSubAdmin =================

    @Test
    void setSubAdmin_success() {
        SubAdminDetailsDTO dto = new SubAdminDetailsDTO();
        dto.setUsername("user");
        dto.setPassword("Password123!");

        Admin admin = new Admin();
        admin.setPending(true);

        when(jwtUtil.extractRole(TOKEN)).thenReturn("USER_ADMIN");
        when(adminRepository.findByRole(AdminRole.USER_ADMIN)).thenReturn(admin);

        assertEquals("Username Set.", adminService.setSubAdmin(dto, TOKEN));
        verify(adminRepository).save(admin);
    }

    @Test
    void setSubAdmin_alreadyUpdated() {
        SubAdminDetailsDTO dto = new SubAdminDetailsDTO();

        Admin admin = new Admin();
        admin.setPending(false);

        when(jwtUtil.extractRole(TOKEN)).thenReturn("USER_ADMIN");
        when(adminRepository.findByRole(AdminRole.USER_ADMIN)).thenReturn(admin);

        assertEquals("SubAdmin Already Updated.",
                adminService.setSubAdmin(dto, TOKEN));
    }

    // ================= changePassword =================

    @Test
    void changePassword_success() {
        Admin admin = new Admin();

        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(admin);

        String result = adminService.changePassword("Password123!", TOKEN);

        assertEquals("Password Changed Successfully.", result);
        verify(adminRepository).save(admin);
    }

    @Test
    void changePassword_adminNotFound() {
        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(null);

        assertThrows(AdminNotFoundException.class,
                () -> adminService.changePassword("Password123!", TOKEN));
    }
}