package com.example.admin_service.controller;

import com.example.admin_service.component.RequiresRole;
import com.example.admin_service.dto.request.AdminLoginDTO;
import com.example.admin_service.dto.response.CourseResponseDTO;
import com.example.admin_service.dto.response.TrainerResponseDTO;
import com.example.admin_service.service.AdminService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> adminLogin(@Valid @RequestBody AdminLoginDTO request){
        log.info("Admin Login Controller hit");
        return ResponseEntity.ok(adminService.login(request));
    }

    @GetMapping("/user/{id}")
    @RequiresRole("ADMIN")
    public ResponseEntity<Object> getTrainer(@RequestHeader("Authorization") String token,
            @PathVariable("id") String id
    ) {
        log.info("Role Accepted");
        Object user = adminService.getUser(token, id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/trainer/verify/{userId}")
    @RequiresRole("ADMIN")
    public ResponseEntity<Object> approveTrainer(@RequestHeader("Authorization") String token,@PathVariable("userId") String trainerId){
        return ResponseEntity.ok(adminService.trainerApprove(token, trainerId));
    }
    @PostMapping("/trainer/reject/{userId}")
    @RequiresRole("ADMIN")
    public ResponseEntity<Object> rejectTrainer(@RequestHeader("Authorization") String token,@PathVariable("userId") String trainerId){
        return ResponseEntity.ok(adminService.rejectApprove(token, trainerId));
    }

    @GetMapping("/pending/trainer")
    @RequiresRole("ADMIN")
    public ResponseEntity<List<Object>> getPendingTrainer(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(adminService.getPendingTrainers());
    }

    @GetMapping("all/trainers")
    @RequiresRole("ADMIN")
    public ResponseEntity<List<TrainerResponseDTO>> getAllTrainer(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(adminService.getAllTrainer(token));
    }

    @PostMapping("all/course")
    @RequiresRole("ADMIN")
    public ResponseEntity<List<CourseResponseDTO>> getAllUnVerified(@RequestHeader("Authorization") String token){
        return ResponseEntity.ok(adminService.getAllUnVerified(token));
    }
    @PostMapping("/course/verify/{courseId}")
    @RequiresRole("ADMIN")
    public ResponseEntity<Object> verifyCourse(@RequestHeader("Authorization") String token,
                                               @PathVariable("courseId") String courseId){
        return ResponseEntity.ok(adminService.verifyCourse(token, courseId));
    }
    @PostMapping("/course/reject/{courseId}")
    @RequiresRole("ADMIN")
    public ResponseEntity<Object> rejectCourse(@RequestHeader("Authorization") String token,
                                               @PathVariable("courseId") String courseId){
        return ResponseEntity.ok(adminService.rejectCourse(token, courseId));
    }
}
