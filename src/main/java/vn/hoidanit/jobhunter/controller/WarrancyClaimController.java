package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;
import vn.hoidanit.jobhunter.domain.ApiResponse;
import vn.hoidanit.jobhunter.domain.ServiceCenter;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.WarrantyClaim;
import vn.hoidanit.jobhunter.service.WarrancyClaimService;

public class WarrancyClaimController {
    private final WarrancyClaimService warrancyClaimService;

    public WarrancyClaimController(WarrancyClaimService warrancyClaimService) {
        this.warrancyClaimService = warrancyClaimService;
    }

    @PostMapping("/Claim")
    public ResponseEntity<ApiResponse<WarrantyClaim>> login(@Valid @AuthenticationPrincipal User user,
            @RequestBody WarrantyClaim input) {
        ServiceCenter serivceCenter = user.getServiceCenter();
        WarrantyClaim warrantyClaim = this.warrancyClaimService.handleCreateClaim(serivceCenter, input);
        var result = new ApiResponse<>(HttpStatus.OK, "Claim successfully", warrantyClaim, null);
        return ResponseEntity.ok(result);
    }
}
