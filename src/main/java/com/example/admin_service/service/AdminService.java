package com.example.admin_service.service;

import com.example.admin_service.dto.request.AdminLoginDTO;
import com.example.admin_service.dto.response.AdminResponseDTO;
import com.example.admin_service.dto.response.CourseResponseDTO;
import com.example.admin_service.dto.response.TrainerResponseDTO;
import com.example.admin_service.exceptions.AdminNotFoundException;
import com.example.admin_service.feign.AuthClient;
import com.example.admin_service.feign.CourseClient;
import com.example.admin_service.feign.UserClient;
import com.example.admin_service.model.Admin;
import com.example.admin_service.repository.AdminRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class AdminService {

    private final UserClient userClient;
    private final AuthClient authClient;
    private final CourseClient courseClient;
    private  final AdminRepository adminRepository;

    public AdminService(UserClient userClient, AuthClient authClient, CourseClient courseClient, AdminRepository adminRepository) {
        this.userClient = userClient;
        this.authClient = authClient;
        this.courseClient = courseClient;
        this.adminRepository = adminRepository;
    }
    public Object getUser(String token, String id) {
        String role = authClient.geUserById(token, id).getRole().name();
        if(Objects.equals(role, "TRAINER")){
            return userClient.getTrainerById(token, id);
        } else if (Objects.equals(role, "ADMIN")) {
            return userClient.getAdmin( id);
        }else{
            return userClient.getLearner(token, id);
        }
    }

    public Object trainerApprove(String token, String trainerId) {
        return authClient.activateTrainer(token, trainerId);
    }

    public Object rejectApprove(String token, String trainerId) {
        return authClient.rejectTrainer(token, trainerId);
    }

    public List<TrainerResponseDTO> getAllTrainer(String token) {
        return userClient.getAllTrainers(token);
    }

    public Object verifyCourse(String token, String courseId) {
        return courseClient.verifyCourse(token, courseId);
    }
    public Object rejectCourse(String token, String courseId){
        return courseClient.rejectCourse(token, courseId);
    }

    public List<CourseResponseDTO> getAllUnVerified(String token) {
        return courseClient.getAllUnVerifiedCourses(token);
    }

    public List<Object> getPendingTrainers() {
        return authClient.getPendingTrainers();
    }

    public Object login(@Valid AdminLoginDTO request) {
        String adminId = request.getAdminId();
        Admin admin = adminRepository.findById(adminId);

        if(admin == null){
            log.warn("Login failed – not found: {}", adminId);
            throw new AdminNotFoundException("Sub-Admin not Found.");
        }
        AdminResponseDTO adminDto = new AdminResponseDTO();
        adminDto.setAdminId(adminId);
        adminDto.setEmail(admin.getEmail());
        adminDto.setAdminRole(admin.getAdminRole());
        adminDto.setUsername(admin.getUsername());
        adminDto.setUserId(admin.getUserId());
        return authClient.adminLogin(request, adminDto);
    }
}
