package com.example.admin_service.service;

import com.example.admin_service.dto.request.*;
import com.example.admin_service.dto.response.AdminLoginRequest;
import com.example.admin_service.dto.response.CourseResponseDTO;
import com.example.admin_service.dto.response.Response;
import com.example.admin_service.dto.response.TrainerResponseDTO;
import com.example.admin_service.enums.AdminRole;
import com.example.admin_service.exceptions.*;
import com.example.admin_service.feign.AuthClient;
import com.example.admin_service.feign.CourseClient;
import com.example.admin_service.feign.PaymentClient;
import com.example.admin_service.feign.UserClient;
import com.example.admin_service.model.Admin;
import com.example.admin_service.repository.AdminRepository;
import com.example.admin_service.util.JwtUtil;
import com.example.admin_service.util.PasswordValidator;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class AdminService {

    private static final String CHARACTERS =
                    "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
                    "abcdefghijklmnopqrstuvwxyz" +
                    "0123456789" +
                    "@#$%&*!";

    private final UserClient userClient;
    private final AuthClient authClient;
    private final CourseClient courseClient;
    private  final PaymentClient paymentClient;
    private  final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private  final SecureRandom random;

    public AdminService(UserClient userClient, AuthClient authClient, CourseClient courseClient, PaymentClient paymentClient, AdminRepository adminRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, SecureRandom random) {
        this.userClient = userClient;
        this.authClient = authClient;
        this.courseClient = courseClient;
        this.paymentClient = paymentClient;
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.random = random;
    }
    public Object getUser(String token, String id) {
        String role = authClient.geUserById(token, id).getRole().name();
        if(Objects.equals(role, "TRAINER")){
            return userClient.getTrainerById(token, id);
        } else if (Objects.equals(role, "SUPER_ADMIN")) {
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

    public List<TrainerResponseDTO> getPendingTrainers(String token) {
        return userClient.getAllPendingTrainers(token);
    }

    public Object login(@Valid AdminLoginDTO request){
        String email = request.getEmail();
        Admin admin = adminRepository.findById(email);

        if(admin == null){
            log.warn("Login failed – not found: {}", email);
            throw new AdminNotFoundException("Sub-Admin not Found.");
        }
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        AdminLoginRequest adminDto = new AdminLoginRequest();
        adminDto.setAdminId(admin.getAdminId());
        adminDto.setEmail(email);
        adminDto.setAdminRole(admin.getAdminRole());
        adminDto.setUsername(admin.getUsername());
        Object object =  authClient.adminLogin(adminDto);
        if(admin.isPending()){
            return new Response<>(false, "Admin Pending.", object);
        }else {
            return object;
        }
    }

    public String subAdminCreate(String token, SubAdminRequest request) {
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 8; i++) {

            int index = random.nextInt(CHARACTERS.length());

            password.append(CHARACTERS.charAt(index));
        }
        Admin admin = new Admin();
        admin.setAdminId(UUID.randomUUID().toString());
        admin.setEmail(request.getEmail());
        PasswordValidator.validate(String.valueOf(password));
        String hashedPassword = BCrypt.hashpw(password.toString(), BCrypt.gensalt(12));
        admin.setPassword(hashedPassword);
        admin.setAdminRole(request.getAdminRole());
        admin.setPending(true);
        adminRepository.save(admin);
        return "Sub Admin Created for Role:" + request.getAdminRole().name();
    }

    public String setSubAdmin(SubAdminDetailsDTO request, String token){
        String role = jwtUtil.extractRole(token);
        Admin admin = adminRepository.findByRole(AdminRole.valueOf(role));
        if(!admin.isPending()){
            return  "SubAdmin Already Updated.";
        }
        admin.setUsername(request.getUsername());
        PasswordValidator.validate(request.getPassword());
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12));
        admin.setPassword(hashedPassword);
        admin.setPending(false);
        adminRepository.save(admin);
        return "Username Set.";
    }
    public String changePassword(String password, String token){
        String role = jwtUtil.extractRole(token);
        Admin admin = adminRepository.findByRole(AdminRole.valueOf(role));
        if(admin == null){
            throw new AdminNotFoundException("Admin Not Found.");
        }

        PasswordValidator.validate(password);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        admin.setPassword(hashedPassword);
        adminRepository.save(admin);
        return "Password Changed Successfully.";
    }

    public List<PayoutRequest> getPendingPayout() {
        try {
            return paymentClient.getPendingPayouts();
        } catch (Exception ex) {
            throw new FetchPendingPayoutException("Failed to fetch pending payouts");
        }
    }

    public String processPayoutRequest(String token, ProcessPayoutRequest request) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedPayoutAccessException("Invalid token");
        }

        try {
            return paymentClient.processPayoutRequest(request, token);
        } catch (Exception ex) {
            throw new PayoutProcessingException("Failed to process payout request");
        }
    }

    public String processPayoutRequestByPath(String token, String action, String payoutId, String remarks) {
        if (!action.equalsIgnoreCase("APPROVE") && !action.equalsIgnoreCase("REJECT")) {
            throw new InvalidPayoutActionException("Invalid action: " + action);
        }

        try {
            return paymentClient.processPayoutRequestByPath(token, action, payoutId, remarks);
        } catch (Exception ex) {
            throw new PaymentClientException("Error while calling payment service");
        }
    }
}
