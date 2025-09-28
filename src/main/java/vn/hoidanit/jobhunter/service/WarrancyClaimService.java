package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.ServiceCenter;
import vn.hoidanit.jobhunter.domain.WarrantyClaim;

@Service
public interface WarrancyClaimService {

    WarrantyClaim handleCreateClaim(ServiceCenter serviceCenter, WarrantyClaim input);
}
