package com.example.admin_service.feign;

import com.example.admin_service.dto.response.AdminLoginRequest;
import com.example.admin_service.dto.request.UserDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthClient {
    @PostMapping("api/v1/auth/internal/trainer/activate/{userId}")
    ResponseEntity<Object> activateTrainer(@RequestHeader("Authorization") String token, @PathVariable String userId);
    @PostMapping("/internal/trainer/reject/{userId}")
    ResponseEntity<Object> rejectTrainer(@RequestHeader("Authorization") String token,@PathVariable String userId);
    @GetMapping("api/v1/auth/status")
    ResponseEntity<Object> checkStatus(
            @RequestParam @Email @NotBlank String email);
    @PostMapping("api/v1/auth/admin/login")
    Object adminLogin(@RequestBody AdminLoginRequest adminResponseDTO);

    @GetMapping("api/v1/auth/pending/trainer")
    List<Object> getPendingTrainers();

    @GetMapping("api/v1/auth/user/id/{userId}")
    UserDTO geUserById(@RequestHeader("Authorization") String token, @PathVariable("userId") String userId);
}
