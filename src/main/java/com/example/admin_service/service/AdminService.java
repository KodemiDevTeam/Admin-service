package com.example.admin_service.service;

import com.example.admin_service.dto.request.*;
import com.example.admin_service.dto.response.AdminLoginRequest;
import com.example.admin_service.dto.response.Response;
import com.example.admin_service.dto.response.TrainerResponseDTO;
import com.example.admin_service.dto.response.TransactionHistoryResponse;
import com.example.admin_service.enums.AdminRole;
import com.example.admin_service.exceptions.*;
import com.example.admin_service.feign.AuthClient;
import com.example.admin_service.feign.CourseClient;
import com.example.admin_service.feign.PaymentClient;
import com.example.admin_service.feign.UserClient;
import com.example.admin_service.service.notification.NotificationPublisher;
import com.example.admin_service.dto.notification.BroadcastNotificationRequest;
import com.example.admin_service.dto.notification.NotificationRequest;
import com.example.admin_service.dto.notification.NotificationChannel;
import com.example.admin_service.dto.notification.NotificationType;
import com.example.admin_service.repository.AdminRateLimitRepository;
import com.example.admin_service.repository.AdminRepository;
import com.example.admin_service.model.Admin;
import com.example.admin_service.util.JwtUtil;
import com.example.admin_service.util.PasswordValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Service
public class AdminService {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    private final UserClient userClient;
    private final AuthClient authClient;
    private final CourseClient courseClient;
    private final PaymentClient paymentClient;
    private final AdminRepository adminRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random;
    private final EmailService emailService;
    private final AdminRateLimitRepository adminRateLimitRepository;
    private final NotificationPublisher notificationPublisher;

    @Autowired
    public AdminService(UserClient userClient, AuthClient authClient, CourseClient courseClient,
                        PaymentClient paymentClient, AdminRepository adminRepository, JwtUtil jwtUtil,
                        PasswordEncoder passwordEncoder, SecureRandom random, EmailService emailService,
                        NotificationPublisher notificationPublisher,
                        AdminRateLimitRepository adminRateLimitRepository) {
        this.userClient = userClient;
        this.authClient = authClient;
        this.courseClient = courseClient;
        this.paymentClient = paymentClient;
        this.adminRepository = adminRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.random = random;
        this.emailService = emailService;
        this.notificationPublisher = notificationPublisher;
        this.adminRateLimitRepository = adminRateLimitRepository;
    }

    public Object getUser(String token, String id) {
        String role = authClient.geUserById(token, id).getRole().name();
        if (Objects.equals(role, "TRAINER")) {
            return userClient.getTrainerById(token, id);
        } else if (Objects.equals(role, "SUPER_ADMIN")) {
            return userClient.getAdmin(id);
        } else {
            return userClient.getLearner(token, id);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "allTrainers", allEntries = true),
            @CacheEvict(value = "pendingTrainers", allEntries = true)
    })
    public Object trainerModeration(String token, String trainerId, String action, String remarks) {
        if (trainerId == null || trainerId.isBlank()) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", userClient.getAllPendingTrainers(token));
            response.put("message", "Pending trainers fetched successfully");
            return response;
        }

        TrainerReviewRequest req = new TrainerReviewRequest();
        req.setAction(action);
        req.setRemarks(remarks);
        Object response = authClient.reviewTrainer(token, trainerId, req);

        try {
            if ("APPROVE".equalsIgnoreCase(action)) {
                NotificationRequest notif = NotificationRequest.builder()
                        .userId(trainerId)
                        .title("Trainer Application Approved")
                        .message("Congratulations! Your trainer application has been approved. You can now start creating courses.")
                        .type(NotificationType.TRAINER_VERIFIED)
                        .channels(List.of(NotificationChannel.EMAIL))
                        .referenceId(trainerId)
                        .referenceType("USER")
                        .build();
                notificationPublisher.publish(notif);
            } else if ("REJECT".equalsIgnoreCase(action)) {
                NotificationRequest notif = NotificationRequest.builder()
                        .userId(trainerId)
                        .title("Trainer Application Rejected")
                        .message("We regret to inform you that your trainer application has been rejected. Reason: " + remarks)
                        .type(NotificationType.TRAINER_REJECTED)
                        .channels(List.of(NotificationChannel.EMAIL))
                        .referenceId(trainerId)
                        .referenceType("USER")
                        .build();
                notificationPublisher.publish(notif);
            }
        } catch (Exception ex) {
            log.error("Failed to send trainer review notification", ex);
        }

        return response;
    }

    @Cacheable(value = "allTrainers")
    public List<TrainerResponseDTO> getAllTrainer(String token) {
        return userClient.getAllTrainers(token);
    }

    @CacheEvict(value = "unverifiedCourses", allEntries = true)
    public Map<String, Object> courseModeration(String token, String courseId, String action, String remarks) {
        log.info("Calling course-service moderation: courseId={}, action={}", courseId, action);

        if (courseId == null || courseId.isBlank()) {
            log.info("Fetching unverified courses for admin");
            List<com.example.admin_service.dto.response.CourseResponseDTO> allCourses = courseClient.getAllCoursesAdmin(token);
            List<com.example.admin_service.dto.response.CourseResponseDTO> pendingCourses = new ArrayList<>();
            if (allCourses != null) {
                for (com.example.admin_service.dto.response.CourseResponseDTO c : allCourses) {
                    if (c != null && "PENDING".equalsIgnoreCase(c.getStatus())) {
                        pendingCourses.add(c);
                    }
                }
            }
            Map<String, Object> response = new HashMap<>();
            response.put("data", pendingCourses);
            response.put("message", "Pending courses fetched successfully");
            return response;
        }

        CourseModerationRequest request = new CourseModerationRequest();
        request.setAction(action);
        request.setRemarks(remarks);

        Map<String, Object> response = courseClient.reviewCourse(token, courseId, request);
        log.info("Course-service moderation response received for courseId={}", courseId);
        return response;
    }

    // Task #6: removed @Valid — validation belongs in the controller layer, not the service
    public Object login(AdminLoginDTO request) {
        String email = request.getEmail();
        Admin admin = adminRepository.findByEmail(email);

        if (admin == null) {
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
        Object object = authClient.adminLogin(adminDto);
        if (admin.isPending()) {
            return new Response<>(false, "Admin Pending.", object);
        } else {
            return object;
        }
    }

    public String subAdminCreate(SubAdminRequest request) {
        // Task #8: password is already a String — no need for String.valueOf() or .toString()
        String password = generatePassword();

        Admin admin = new Admin();
        admin.setAdminId(UUID.randomUUID().toString());
        admin.setEmail(request.getEmail());
        PasswordValidator.validate(password);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        admin.setPassword(hashedPassword);
        admin.setUsername(request.getUsername());
        admin.setAdminRole(request.getAdminRole());
        admin.setPending(true);
        emailService.sendOEmail(request.getEmail(), password, request.getUsername());
        adminRepository.save(admin);
        return "Sub Admin Created for Role:" + request.getAdminRole().name();
    }

    public String setSubAdmin(SubAdminDetailsDTO request, String token) {
        String adminId = jwtUtil.extractUserId(token);
        Admin admin = adminRepository.findById(adminId);
        if (!admin.isPending()) {
            return "SubAdmin Already Updated.";
        }
        admin.setUsername(request.getUsername());
        PasswordValidator.validate(request.getPassword());
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12));
        admin.setPassword(hashedPassword);
        admin.setPending(false);
        adminRepository.save(admin);
        return "Username Set.";
    }

    public String changePassword(String password, String token) {
        String role = jwtUtil.extractRole(token);
        Admin admin = adminRepository.findByRole(AdminRole.valueOf(role));
        if (admin == null) {
            throw new AdminNotFoundException("Admin Not Found.");
        }
        PasswordValidator.validate(password);
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        admin.setPassword(hashedPassword);
        adminRepository.save(admin);
        return "Password Changed Successfully.";
    }

    @Cacheable(value = "pendingPayouts")
    public List<PayoutRequest> getAllPayouts(String token) {
        try {
            log.info("Calling Payment service for the response..");
            return paymentClient.getAllPayouts(token);
        } catch (Exception ex) {
            // Task #3: replaced ex.printStackTrace() with log.error()
            log.error("Failed to fetch pending payouts", ex);
            throw new FetchPendingPayoutException("Failed to fetch proccessed payouts");
        }
    }

    @Cacheable(value = "pendingPayouts")
    public TransactionHistoryResponse getAllTransactionHistory() {
        try {
            log.info("Calling Payment Service for the response...");
            return paymentClient.getTransactionHistory();
        } catch (Exception ex) {
            // Task #3: replaced ex.printStackTrace() with log.error()
            log.error("Failed to fetch transaction history", ex);
            throw new FetchPendingPayoutException("Failed to fetch all transactions.");
        }
    }

    @CacheEvict(value = "pendingPayouts", allEntries = true)
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

    @CacheEvict(value = "pendingPayouts", allEntries = true)
    public String processPayoutRequestByPath(String token, String action, String payoutId, String remarks) {
        if (!action.equalsIgnoreCase("APPROVE") && !action.equalsIgnoreCase("REJECT") && !action.equalsIgnoreCase("HOLD")) {
            throw new InvalidPayoutActionException("Invalid action: " + action);
        }
        try {
            return paymentClient.processPayoutRequestByPath(token, action, payoutId, remarks);
        } catch (Exception ex) {
            // Task #3: replaced ex.printStackTrace() with log.error()
            log.error("Error while calling payment service", ex);
            throw new PaymentClientException("Error while calling payment service");
        }
    }

    public String generatePassword() {
        StringBuilder password = new StringBuilder();

        // Ensure all required character types are present
        password.append(UPPER.charAt(random.nextInt(UPPER.length())));
        password.append(LOWER.charAt(random.nextInt(LOWER.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(random.nextInt(SPECIAL.length())));

        // Fill remaining characters
        for (int i = 4; i < 8; i++) {
            password.append(ALL.charAt(random.nextInt(ALL.length())));
        }

        // Task #5: removed redundant null check — StringBuilder is never null
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < password.length(); i++) {
            chars.add(password.charAt(i));
        }

        Collections.shuffle(chars);

        StringBuilder finalPassword = new StringBuilder();
        for (Character c : chars) {
            finalPassword.append(c);
        }

        return finalPassword.toString();
    }

    public String broadcastAnnouncement(String token, AdminBroadcastRequest request) {
        String role = jwtUtil.extractRole(token);
        String adminId = jwtUtil.extractUserId(token);

        String today = java.time.LocalDate.now().toString();
        String rateLimitKey = adminId + "_" + today;
        com.example.admin_service.model.AdminRateLimit rateLimit = adminRateLimitRepository.getAdminRateLimit(rateLimitKey);

        if (rateLimit == null) {
            rateLimit = new com.example.admin_service.model.AdminRateLimit();
            rateLimit.setAdminIdDate(rateLimitKey);
            rateLimit.setBroadcastCount(0);
        }

        if (rateLimit.getBroadcastCount() >= 5) {
            // Task #7: replaced raw RuntimeException with specific BroadcastException
            throw new BroadcastException("Rate limit exceeded: Max 5 broadcasts per day allowed.");
        }

        try {
            List<String> finalChannels = new ArrayList<>(request.getChannels());
            if (!request.isUrgent()) {
                finalChannels.remove("SMS");
            }
            if (finalChannels.contains("SMS") && !"SUPER_ADMIN".equals(role)) {
                // Task #7: replaced raw RuntimeException with specific BroadcastException
                throw new BroadcastException("Only SUPER_ADMIN can send SMS broadcasts");
            }
            if (!finalChannels.contains("IN_APP")) {
                finalChannels.add("IN_APP");
            }

            List<NotificationChannel> channels = new ArrayList<>();
            for (String ch : finalChannels) {
                channels.add(NotificationChannel.valueOf(ch));
            }

            BroadcastNotificationRequest notif = BroadcastNotificationRequest.builder()
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type(NotificationType.ADMIN_BROADCAST)
                    .channels(channels)
                    .targetRole(request.getTargetRole() != null && !request.getTargetRole().equals("ALL")
                            ? request.getTargetRole() : null)
                    .sendMode(request.getTargetRole() != null && !request.getTargetRole().equals("ALL")
                            ? "ROLE_BASED" : "ALL_USERS")
                    .referenceId(UUID.randomUUID().toString())
                    .referenceType("BROADCAST")
                    .build();
            notificationPublisher.publishBroadcast(notif);

            rateLimit.setBroadcastCount(rateLimit.getBroadcastCount() + 1);
            adminRateLimitRepository.save(rateLimit);

            return "Broadcast sent successfully";
        } catch (BroadcastException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to send broadcast", ex);
            // Task #7: replaced raw RuntimeException with specific BroadcastException
            throw new BroadcastException("Failed to send broadcast", ex);
        }
    }

    public String suspendUser(String userId, String reason) {
        try {
            NotificationRequest notif = NotificationRequest.builder()
                    .userId(userId)
                    .title("Account Suspended")
                    .message("Your account has been suspended by the admin. Reason: " + reason)
                    .type(NotificationType.ACCOUNT_SUSPENDED)
                    .channels(List.of(NotificationChannel.IN_APP, NotificationChannel.EMAIL))
                    .referenceId(userId + "_suspend_" + System.currentTimeMillis())
                    .referenceType("USER")
                    .build();
            notificationPublisher.publish(notif);
            return "User suspended and notified successfully";
        } catch (Exception ex) {
            log.error("Failed to notify suspended user", ex);
            // Task #7: replaced raw RuntimeException with specific UserSuspendException
            throw new UserSuspendException("Failed to notify user", ex);
        }
    }
}
