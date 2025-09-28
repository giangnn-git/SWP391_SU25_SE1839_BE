package vn.hoidanit.jobhunter.service.Impl;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.ServiceCenter;
import vn.hoidanit.jobhunter.domain.WarrantyClaim;
import vn.hoidanit.jobhunter.repository.VehicleRepository;
import vn.hoidanit.jobhunter.repository.WarrancyClaimRepository;
import vn.hoidanit.jobhunter.service.WarrancyClaimService;

@Service
public class WarrancyClaimServiceImpl implements WarrancyClaimService {

    private final WarrancyClaimRepository warrancyClaimRepository;
    private final VehicleRepository vehicleRepository;

    public WarrancyClaimServiceImpl(WarrancyClaimRepository warrancyClaimRepository,
            VehicleRepository vehicleRepository) {
        this.warrancyClaimRepository = warrancyClaimRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public WarrantyClaim handleCreateClaim(ServiceCenter serviceCenter, WarrantyClaim input) {
        if (this.vehicleRepository.findByVin(input.getVehicle().getVin()) == null) {
            throw new IllegalArgumentException("Xe với VIN " + input.getVehicle().getVin() + " không tồn tại");
        }
        input.setServiceCenter(serviceCenter);
        return this.warrancyClaimRepository.save(input);
    }

}