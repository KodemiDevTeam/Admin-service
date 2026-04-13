package com.example.admin_service.feign;

import com.example.admin_service.dto.request.PayoutRequest;
import com.example.admin_service.dto.request.ProcessPayoutRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "payment-service")
public interface PaymentClient {
    @GetMapping("/payouts/pending")
    List<PayoutRequest> getPendingPayouts();
    @PostMapping("/payouts/process")
    String processPayoutRequest(
            @RequestBody ProcessPayoutRequest request,
            @RequestHeader("Authorization") String token);
    @PostMapping("/payouts/process/{payoutId}")
    String processPayoutRequestByPath(
            @PathVariable String payoutId,
            @RequestParam String action, // APPROVE or REJECT
            @RequestParam(required = false) String remarks,
            @RequestHeader("Authorization") String token);
}
