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

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Created", response.getBody());
        verify(adminService).subAdminCreate(TOKEN, request);
    }

    @Test
    void subAdmin_nullResponse() {
        SubAdminRequest request = new SubAdminRequest();

        when(adminService.subAdminCreate(TOKEN, request)).thenReturn(null);

        ResponseEntity<String> response = adminController.subAdmin(TOKEN, request);

        assertNull(response.getBody());
        verify(adminService).subAdminCreate(TOKEN, request);
    }

    // ===== UPDATE SUB ADMIN =====
    @Test
    void setSubAdmin_success() {
        SubAdminDetailsDTO request = new SubAdminDetailsDTO();

        when(adminService.setSubAdmin(request, TOKEN)).thenReturn("Updated");

        ResponseEntity<Object> response = adminController.setSubAdmin(request, TOKEN);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated", response.getBody());
        verify(adminService).setSubAdmin(request, TOKEN);
    }

    @Test
    void setSubAdmin_null() {
        SubAdminDetailsDTO request = new SubAdminDetailsDTO();

        when(adminService.setSubAdmin(request, TOKEN)).thenReturn(null);

        ResponseEntity<Object> response = adminController.setSubAdmin(request, TOKEN);

        assertNull(response.getBody());
        verify(adminService).setSubAdmin(request, TOKEN);
    }

    // ===== CHANGE PASSWORD =====
    @Test
    void changePassword_success() {
        PasswordChange request = new PasswordChange();
        request.setPassword("newPass");

        when(adminService.changePassword("newPass", TOKEN)).thenReturn("Changed");

        ResponseEntity<String> response = adminController.updatePassword(request, TOKEN);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Changed", response.getBody());
        verify(adminService).changePassword("newPass", TOKEN);
    }

    @Test
    void changePassword_exception() {
        PasswordChange request = new PasswordChange();
        request.setPassword("bad");

        when(adminService.changePassword("bad", TOKEN))
                .thenThrow(new RuntimeException("Error"));

        assertThrows(RuntimeException.class,
                () -> adminController.updatePassword(request, TOKEN));
    }

    // ===== LOGIN =====
    @Test
    void login_success() {
        AdminLoginDTO request = new AdminLoginDTO();

        when(adminService.login(request)).thenReturn("token");

        ResponseEntity<Object> response = adminController.adminLogin(request);

        assertEquals("token", response.getBody());
        verify(adminService).login(request);
    }

    @Test
    void login_exception() {
        AdminLoginDTO request = new AdminLoginDTO();

        when(adminService.login(request)).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> adminController.adminLogin(request));
    }

    // ===== GET USER =====
    @Test
    void getUser_success() {
        when(adminService.getUser(TOKEN, "1")).thenReturn("data");

        ResponseEntity<Object> response = adminController.getTrainer(TOKEN, "1");

        assertEquals("data", response.getBody());
        verify(adminService).getUser(TOKEN, "1");
    }

    @Test
    void getUser_null() {
        when(adminService.getUser(TOKEN, "1")).thenReturn(null);

        ResponseEntity<Object> response = adminController.getTrainer(TOKEN, "1");

        assertNull(response.getBody());
        verify(adminService).getUser(TOKEN, "1");
    }

    // ===== TRAINER =====
    @Test
    void approveTrainer() {
        when(adminService.trainerApprove(TOKEN, "t1")).thenReturn("ok");

        ResponseEntity<Object> response =
                adminController.approveTrainer(TOKEN, "t1");

        assertEquals("ok", response.getBody());
        verify(adminService).trainerApprove(TOKEN, "t1");
    }

    @Test
    void rejectTrainer() {
        when(adminService.rejectApprove(TOKEN, "t1")).thenReturn("no");

        ResponseEntity<Object> response =
                adminController.rejectTrainer(TOKEN, "t1");

        assertEquals("no", response.getBody());
        verify(adminService).rejectApprove(TOKEN, "t1");
    }

    // ===== TRAINER LIST =====
    @Test
    void getPendingTrainer() {
        List<TrainerResponseDTO> list = List.of();

        when(adminService.getPendingTrainers(TOKEN)).thenReturn(list);

        ResponseEntity<List<TrainerResponseDTO>> response =
                adminController.getPendingTrainer(TOKEN);

        assertEquals(list, response.getBody());
        verify(adminService).getPendingTrainers(TOKEN);
    }

    @Test
    void getAllTrainer() {
        List<TrainerResponseDTO> list = List.of();

        when(adminService.getAllTrainer(TOKEN)).thenReturn(list);

        ResponseEntity<List<TrainerResponseDTO>> response =
                adminController.getAllTrainer(TOKEN);

        assertEquals(list, response.getBody());
        verify(adminService).getAllTrainer(TOKEN);
    }

    // ===== COURSE =====
    @Test
    void getAllUnverified() {
        List<CourseResponseDTO> list = List.of();

        when(adminService.getAllUnVerified(TOKEN)).thenReturn(list);

        ResponseEntity<List<CourseResponseDTO>> response =
                adminController.getAllUnVerified(TOKEN);

        assertEquals(list, response.getBody());
        verify(adminService).getAllUnVerified(TOKEN);
    }

    @Test
    void verifyCourse() {
        when(adminService.verifyCourse(TOKEN, "c1")).thenReturn("done");

        ResponseEntity<Object> response =
                adminController.verifyCourse(TOKEN, "c1");

        assertEquals("done", response.getBody());
        verify(adminService).verifyCourse(TOKEN, "c1");
    }

    @Test
    void rejectCourse() {
        when(adminService.rejectCourse(TOKEN, "c1")).thenReturn("fail");

        ResponseEntity<Object> response =
                adminController.rejectCourse(TOKEN, "c1");

        assertEquals("fail", response.getBody());
        verify(adminService).rejectCourse(TOKEN, "c1");
    }

    // ===== PAYOUT =====
    @Test
    void getPendingPayouts() {
        List<PayoutRequest> list = List.of(new PayoutRequest());

        when(adminService.getPendingPayout()).thenReturn(list);

        ResponseEntity<List<PayoutRequest>> response =
                adminController.getPendingPayouts(TOKEN);

        assertEquals(list, response.getBody());
        verify(adminService).getPendingPayout();
    }

    @Test
    void getPendingPayouts_empty() {
        when(adminService.getPendingPayout()).thenReturn(List.of());

        ResponseEntity<List<PayoutRequest>> response =
                adminController.getPendingPayouts(TOKEN);

        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void processPayoutRequest_success() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();

        when(adminService.processPayoutRequest(TOKEN, request))
                .thenReturn("processed");

        ResponseEntity<String> response =
                adminController.processPayoutRequest(TOKEN, request);

        assertEquals("processed", response.getBody());
        verify(adminService).processPayoutRequest(TOKEN, request);
    }

    @Test
    void processPayoutRequest_exception() {
        ProcessPayoutRequest request = new ProcessPayoutRequest();

        when(adminService.processPayoutRequest(TOKEN, request))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> adminController.processPayoutRequest(TOKEN, request));
    }

    @Test
    void processPayoutRequestByPath_success() {
        when(adminService.processPayoutRequestByPath(TOKEN, "APPROVE", "p1", "ok"))
                .thenReturn("done");

        ResponseEntity<String> response =
                adminController.processPayoutRequestByPath("p1", "APPROVE", "ok", TOKEN);

        assertEquals("done", response.getBody());
        verify(adminService).processPayoutRequestByPath(TOKEN, "APPROVE", "p1", "ok");
    }

    @Test
    void processPayoutRequestByPath_nullRemarks() {
        when(adminService.processPayoutRequestByPath(TOKEN, "REJECT", "p1", null))
                .thenReturn("rejected");

        ResponseEntity<String> response =
                adminController.processPayoutRequestByPath("p1", "REJECT", null, TOKEN);

        assertEquals("rejected", response.getBody());
    }

    @Test
    void processPayoutRequestByPath_exception() {
        when(adminService.processPayoutRequestByPath(any(), any(), any(), any()))
                .thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class,
                () -> adminController.processPayoutRequestByPath("p1", "APPROVE", "ok", TOKEN));
    }
}