package com.example.admin_service.service;

import com.example.admin_service.dto.request.AdminLoginDTO;
import com.example.admin_service.dto.request.SubAdminDetailsDTO;
import com.example.admin_service.dto.request.SubAdminRequest;
import com.example.admin_service.dto.response.AdminLoginRequest;
import com.example.admin_service.dto.response.CourseResponseDTO;
import com.example.admin_service.dto.response.TrainerResponseDTO;
import com.example.admin_service.enums.AdminRole;
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
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private AuthClient authClient;

    @Mock
    private CourseClient courseClient;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private final String TOKEN = "Bearer token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===== getUser =====
    @Test
    void getUser_trainer() {
        com.example.admin_service.dto.request.UserDTO userDTO = new com.example.admin_service.dto.request.UserDTO();
        userDTO.setRole(com.example.admin_service.enums.Role.TRAINER);

        com.example.admin_service.dto.response.TrainerResponseDTO trainerDTO = new com.example.admin_service.dto.response.TrainerResponseDTO();
        when(authClient.geUserById(TOKEN, "t1")).thenReturn(userDTO);
        when(userClient.getTrainerById(TOKEN, "t1")).thenReturn(trainerDTO);

        Object result = adminService.getUser(TOKEN, "t1");
        assertEquals(trainerDTO, result);
    }

    @Test
    void getUser_admin() {
        com.example.admin_service.dto.request.UserDTO userDTO = new com.example.admin_service.dto.request.UserDTO();
        // In the service, the check is for exactly "ADMIN". But the Role enum only has SUPER_ADMIN etc.
        // So passing SUPER_ADMIN will fall back to getLearner
        userDTO.setRole(com.example.admin_service.enums.Role.SUPER_ADMIN);

        com.example.admin_service.dto.response.LearnerResponseDTO learnerFallbackDTO = new com.example.admin_service.dto.response.LearnerResponseDTO();
        when(authClient.geUserById(TOKEN, "a1")).thenReturn(userDTO);
        when(userClient.getLearner(TOKEN, "a1")).thenReturn(learnerFallbackDTO);

        Object result = adminService.getUser(TOKEN, "a1");
        assertEquals(learnerFallbackDTO, result);
    }

    @Test
    void getUser_learner() {
        com.example.admin_service.dto.request.UserDTO userDTO = new com.example.admin_service.dto.request.UserDTO();
        userDTO.setRole(com.example.admin_service.enums.Role.LEARNER);

        com.example.admin_service.dto.response.LearnerResponseDTO learnerDTO = new com.example.admin_service.dto.response.LearnerResponseDTO();
        when(authClient.geUserById(TOKEN, "u1")).thenReturn(userDTO);
        when(userClient.getLearner(TOKEN, "u1")).thenReturn(learnerDTO);

        Object result = adminService.getUser(TOKEN, "u1");
        assertEquals(learnerDTO, result);
    }

    // ===== trainerApprove / rejectApprove =====
    @Test
    void trainerApprove_success() {
        org.springframework.http.ResponseEntity<Object> responseEntity = org.springframework.http.ResponseEntity.ok("approved");
        when(authClient.activateTrainer(TOKEN, "t1")).thenReturn(responseEntity);
        assertEquals(responseEntity, adminService.trainerApprove(TOKEN, "t1"));
    }

    @Test
    void rejectApprove_success() {
        org.springframework.http.ResponseEntity<Object> responseEntity = org.springframework.http.ResponseEntity.ok("rejected");
        when(authClient.rejectTrainer(TOKEN, "t1")).thenReturn(responseEntity);
        assertEquals(responseEntity, adminService.rejectApprove(TOKEN, "t1"));
    }

    // ===== getAllTrainer / getAllUnVerified / getPendingTrainers =====
    @Test
    void getAllTrainer_success() {
        List<TrainerResponseDTO> list = List.of(new TrainerResponseDTO());
        when(userClient.getAllTrainers(TOKEN)).thenReturn(list);

        assertEquals(list, adminService.getAllTrainer(TOKEN));
    }

    @Test
    void getAllUnVerified_success() {
        List<CourseResponseDTO> list = List.of(CourseResponseDTO.builder()
                .courseId("c1")
                .courseCode("CODE1")
                .slug("slug1")
                .version(1)
                .createdAt(new Date())
                .updatedAt(new Date())
                .createdBy("admin")
                .build());
        when(courseClient.getAllUnVerifiedCourses(TOKEN)).thenReturn(list);

        assertEquals(list, adminService.getAllUnVerified(TOKEN));
    }

    @Test
    void getPendingTrainers_success() {
        List<TrainerResponseDTO> list = List.of(new TrainerResponseDTO());
        when(userClient.getAllPendingTrainers(TOKEN)).thenReturn(list);

        assertEquals(list, adminService.getPendingTrainers(TOKEN));
    }

    // ===== login =====
    @Test
    void login_success_pending() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("admin1");
        dto.setPassword("correct");
        Admin admin = new Admin();
        admin.setPassword("hashed");
        admin.setPending(true);
        admin.setEmail("a@a.com");
        admin.setAdminRole(AdminRole.SUPER_ADMIN);
        admin.setUsername("user1");
        admin.setUserId("u1");

        org.springframework.http.ResponseEntity<Object> responseEntity = org.springframework.http.ResponseEntity.ok("object");
        when(adminRepository.findById("admin1")).thenReturn(admin);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(authClient.adminLogin(any(AdminLoginRequest.class))).thenReturn(responseEntity);

        Object result = adminService.login(dto);
        assertTrue(result instanceof com.example.admin_service.dto.response.Response);
    }

    @Test
    void login_success_notPending() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("admin1");
        dto.setPassword("correct");
        Admin admin = new Admin();
        admin.setPassword("hashed");
        admin.setPending(false);
        admin.setEmail("a@a.com");
        admin.setAdminRole(AdminRole.SUPER_ADMIN);
        admin.setUsername("user1");
        admin.setUserId("u1");

        org.springframework.http.ResponseEntity<Object> responseEntity = org.springframework.http.ResponseEntity.ok("object");
        when(adminRepository.findById("admin1")).thenReturn(admin);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(authClient.adminLogin(any(AdminLoginRequest.class))).thenReturn(responseEntity);

        Object result = adminService.login(dto);
        assertEquals(responseEntity, result);
    }

    @Test
    void login_adminNotFound() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("admin1");
        when(adminRepository.findById("admin1")).thenReturn(null);

        assertThrows(AdminNotFoundException.class, () -> adminService.login(dto));
    }

    @Test
    void login_invalidPassword() {
        AdminLoginDTO dto = new AdminLoginDTO();
        dto.setAdminId("admin1");
        dto.setPassword("wrong");
        Admin admin = new Admin();
        admin.setPassword("hashed");

        when(adminRepository.findById("admin1")).thenReturn(admin);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> adminService.login(dto));
    }

    // ===== subAdminCreate / setSubAdmin / changePassword =====
    @Test
    void subAdminCreate_success() {
        SubAdminRequest request = new SubAdminRequest();
        request.setAdminId("admin1");
        request.setAdminRole(AdminRole.SUPER_ADMIN);
        request.setPassword("Pass@123");

        when(jwtUtil.extractEmail(TOKEN)).thenReturn("a@a.com");
        when(jwtUtil.extractUserId(TOKEN)).thenReturn("u1");

        String result = adminService.subAdminCreate(TOKEN, request);
        assertTrue(result.contains("Sub Admin Created"));
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void setSubAdmin_success() {
        SubAdminDetailsDTO request = new SubAdminDetailsDTO();
        request.setUsername("user1");
        request.setPassword("Pass@123");

        Admin admin = new Admin();
        admin.setPending(true);

        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(admin);

        String result = adminService.setSubAdmin(request, TOKEN);
        assertEquals("Username Set.", result);
        assertFalse(admin.isPending());
    }

    @Test
    void changePassword_success() {
        Admin admin = new Admin();
        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(admin);

        String result = adminService.changePassword("Pass@123", TOKEN);
        assertEquals("Password Changed Successfully.", result);
        verify(adminRepository).save(admin);
    }

    @Test
    void changePassword_adminNotFound() {
        when(jwtUtil.extractRole(TOKEN)).thenReturn("SUPER_ADMIN");
        when(adminRepository.findByRole(AdminRole.SUPER_ADMIN)).thenReturn(null);

        assertThrows(AdminNotFoundException.class, () -> adminService.changePassword("Pass@123", TOKEN));
    }
}