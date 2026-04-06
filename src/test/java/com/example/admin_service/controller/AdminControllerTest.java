package com.example.admin_service.controller;

import com.example.admin_service.dto.request.*;
import com.example.admin_service.dto.response.CourseResponseDTO;
import com.example.admin_service.dto.response.TrainerResponseDTO;
import com.example.admin_service.service.AdminService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private final String TOKEN = "Bearer token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===== CREATE SUB ADMIN =====
    @Test
    void subAdmin_success() {
        SubAdminRequest request = new SubAdminRequest();

        when(adminService.subAdminCreate(TOKEN, request)).thenReturn("Created");

        ResponseEntity<String> response = adminController.subAdmin(TOKEN, request);

        assertEquals("Created", response.getBody());
    }

    // ===== UPDATE SUB ADMIN =====
    @Test
    void setSubAdmin_success() {
        SubAdminDetailsDTO request = new SubAdminDetailsDTO();

        when(adminService.setSubAdmin(request, TOKEN)).thenReturn("Updated");

        ResponseEntity<Object> response = adminController.setSubAdmin(request, TOKEN);

        assertEquals("Updated", response.getBody());
    }

    // ===== CHANGE PASSWORD =====
    @Test
    void changePassword_success() {
        PasswordChange request = new PasswordChange();
        request.setPassword("newPass");

        when(adminService.changePassword("newPass", TOKEN)).thenReturn("Changed");

        ResponseEntity<String> response = adminController.updatePassword(request, TOKEN);

        assertEquals("Changed", response.getBody());
    }

    // ===== LOGIN =====
    @Test
    void login_success() {
        AdminLoginDTO request = new AdminLoginDTO();

        when(adminService.login(request)).thenReturn("token");

        ResponseEntity<Object> response = adminController.adminLogin(request);

        assertEquals("token", response.getBody());
    }

    // ===== GET USER =====
    @Test
    void getUser_success() {
        String userId = "user123";

        when(adminService.getUser(TOKEN, userId)).thenReturn("userData");

        ResponseEntity<Object> response = adminController.getTrainer(TOKEN, userId);

        assertEquals("userData", response.getBody());
    }

    // ===== APPROVE TRAINER =====
    @Test
    void approveTrainer_success() {
        String trainerId = "trainer1";

        when(adminService.trainerApprove(TOKEN, trainerId)).thenReturn("approved");

        ResponseEntity<Object> response = adminController.approveTrainer(TOKEN, trainerId);

        assertEquals("approved", response.getBody());
    }

    // ===== REJECT TRAINER =====
    @Test
    void rejectTrainer_success() {
        String trainerId = "trainer1";

        when(adminService.rejectApprove(TOKEN, trainerId)).thenReturn("rejected");

        ResponseEntity<Object> response = adminController.rejectTrainer(TOKEN, trainerId);

        assertEquals("rejected", response.getBody());
    }

    // ===== GET PENDING TRAINERS =====
    @Test
    void getPendingTrainer_success() {
        List<TrainerResponseDTO> list = List.of(new TrainerResponseDTO());

        when(adminService.getPendingTrainers(TOKEN)).thenReturn(list);

        ResponseEntity<List<TrainerResponseDTO>> response =
                adminController.getPendingTrainer(TOKEN);

        assertEquals(list, response.getBody());
    }

    // ===== GET ALL TRAINERS =====
    @Test
    void getAllTrainer_success() {
        List<TrainerResponseDTO> list = List.of(new TrainerResponseDTO());

        when(adminService.getAllTrainer(TOKEN)).thenReturn(list);

        ResponseEntity<List<TrainerResponseDTO>> response =
                adminController.getAllTrainer(TOKEN);

        assertEquals(list, response.getBody());
    }

    // ===== GET ALL UNVERIFIED COURSES =====
    @Test
    void getAllUnverified_success() {
        CourseResponseDTO dto = CourseResponseDTO.builder()
                .courseId("c1")
                .courseCode("JAVA101")
                .slug("java-course")
                .version(1)
                .createdAt(new java.util.Date())
                .updatedAt(new java.util.Date())
                .createdBy("admin")
                .build();

        List<CourseResponseDTO> list = List.of(dto);

        when(adminService.getAllUnVerified(TOKEN)).thenReturn(list);

        ResponseEntity<List<CourseResponseDTO>> response =
                adminController.getAllUnVerified(TOKEN);

        assertEquals(list, response.getBody());
    }

    // ===== VERIFY COURSE =====
    @Test
    void verifyCourse_success() {
        String courseId = "course1";

        when(adminService.verifyCourse(TOKEN, courseId)).thenReturn("verified");

        ResponseEntity<Object> response =
                adminController.verifyCourse(TOKEN, courseId);

        assertEquals("verified", response.getBody());
    }

    // ===== REJECT COURSE =====
    @Test
    void rejectCourse_success() {
        String courseId = "course1";

        when(adminService.rejectCourse(TOKEN, courseId)).thenReturn("rejected");

        ResponseEntity<Object> response =
                adminController.rejectCourse(TOKEN, courseId);

        assertEquals("rejected", response.getBody());
    }
}