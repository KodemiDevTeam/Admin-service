package com.example.admin_service.feign;

import com.example.admin_service.dto.response.CourseResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "course-service")
public interface CourseClient {
	
    @GetMapping("/api/v1/course/all/unverified")
    List<CourseResponseDTO> getAllUnVerifiedCourses(@RequestHeader("Authorization") String token);
    
    @PostMapping("/api/v1/course/internal/reject/{courseId}")
    ResponseEntity<Object> rejectCourse(@RequestHeader("Authorization") String token, @PathVariable("courseId") String courseId);

    @PostMapping("/api/v1/course/internal/activate/{courseId}")
    ResponseEntity<Object> verifyCourse(@RequestHeader("Authorization") String token, @PathVariable("courseId") String courseId);

    }
