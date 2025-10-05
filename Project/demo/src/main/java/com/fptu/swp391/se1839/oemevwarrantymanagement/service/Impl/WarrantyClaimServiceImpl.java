package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardClaimSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryItemResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ClaimAttachment;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPriceHistory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartPriceHistoryRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrancyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarrantyClaimServiceImpl implements WarrantyClaimService {
        private final WarrancyClaimRepository warrancyClaimRepository;
        private final PartRepository partRepository;
        private final VehicleRepository vehicleRepository;
        private final ServiceCenterRepository serviceCenterRepository;
        private final PartPriceHistoryRepository partPriceHistoryRepository;

        public DashboardClaimSummaryResponse handleSummaryClaims(Long serviceCenterId) {
                long count = this.warrancyClaimRepository.countByServiceCenterId(serviceCenterId);
                long emegencyCount = this.warrancyClaimRepository.countByServiceCenterIdAndPriority(serviceCenterId,
                                WarrantyClaim.ClaimPriority.URGENT);

                return DashboardClaimSummaryResponse.builder()
                                .count(count)
                                .emegency(emegencyCount)
                                .build();
        }

        private Vehicle getVehicleByVin(String vin) {
                Vehicle vehicle = this.vehicleRepository.findByVin(vin);
                if (vehicle == null) {
                        throw new NoSuchElementException("Vehicle not found with vin " + vin);
                }
                return vehicle;
        }

        private ServiceCenter getServiceCenterById(long id) {
                return this.serviceCenterRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Service center not found with id " + id));
        }

        private Set<PartClaim> buildPartClaims(Set<PartClaimRequest> requests, WarrantyClaim claim) {
                Set<PartClaim> partClaims = new HashSet<>();
                for (PartClaimRequest pcr : requests) {
                        Part part = this.partRepository.findById(pcr.getId())
                                        .orElseThrow(() -> new NoSuchElementException(
                                                        "Part not found with id " + pcr.getId()));

                        PartClaim pc = PartClaim.builder()
                                        .quantity(pcr.getQuantity())
                                        .part(part)
                                        .warrantyClaim(claim)
                                        .build();

                        partClaims.add(pc);
                }
                return partClaims;
        }

        private static void checkDuplicatePartClaim(WarrantyClaim warrantyClaim, List<WarrantyClaim> existingClaims) {
                for (PartClaim newPart : warrantyClaim.getPartClaims()) {
                        boolean exists = existingClaims.stream()
                                        .flatMap(c -> c.getPartClaims().stream())
                                        .anyMatch(pc -> pc.getPart().getId().equals(newPart.getPart().getId()));

                        if (exists) {
                                throw new IllegalArgumentException(
                                                "Part " + newPart.getPart().getName()
                                                                + " already exists for this vehicle on the same date.");
                        }
                }
        }

        @Override
        public CreateClaimResponse handleCreateClaim(CreateClaimRequest request, long serviceCenterId,
                        MultipartFile[] attachments)
                        throws IOException {

                WarrantyClaim warrantyClaim = WarrantyClaim.builder()
                                .description(request.getDescription())
                                .mileage(request.getMileage())
                                .vehicle(getVehicleByVin(request.getVin()))
                                .serviceCenter(getServiceCenterById(serviceCenterId))
                                .claimAttachments(new ArrayList<>()) // 🚀 đảm bảo list luôn tồn tại
                                .build();

                warrantyClaim.setPartClaims(buildPartClaims(request.getPartClaims(), warrantyClaim));

                List<WarrantyClaim> existingClaims = this.warrancyClaimRepository
                                .findByVehicleVinAndClaimDate(
                                                warrantyClaim.getVehicle().getVin(),
                                                warrantyClaim.getClaimDate());
                checkDuplicatePartClaim(warrantyClaim, existingClaims);

                if (attachments != null) {
                        for (MultipartFile file : attachments) {
                                if (!file.isEmpty()) {
                                        ClaimAttachment attachment = ClaimAttachment.builder()
                                                        .name(file.getOriginalFilename())
                                                        .type(file.getContentType())
                                                        .imageData(file.getBytes())
                                                        .warrantyClaim(warrantyClaim)
                                                        .build();

                                        warrantyClaim.getClaimAttachments().add(attachment);
                                }
                        }
                }

                this.warrancyClaimRepository.save(warrantyClaim);

                return CreateClaimResponse.builder()
                                .sccuess("success")
                                .message("Registered claim successfully")
                                .build();
        }

        private long getAllClaims(long serviceCenterId) {
                return warrancyClaimRepository.countByServiceCenterId(serviceCenterId);
        }

        private long getClaimsByStatus(long serviceCenterId, WarrantyClaim.ClaimStatus status) {
                return warrancyClaimRepository.countByServiceCenterIdAndStatus(serviceCenterId, status);
        }

        private double calculateEstimatedCost() {
                List<WarrantyClaim> warrantyClaims = warrancyClaimRepository.findAll();
                double price = 0;
                for (WarrantyClaim wc : warrantyClaims) {
                        for (PartClaim pc : wc.getPartClaims()) {
                                PartPriceHistory pph = partPriceHistoryRepository
                                                .findCurrentPrice(pc.getPart().getId(), wc.getClaimDate())
                                                .orElseThrow(() -> new RuntimeException(
                                                                "No price found for part " + pc.getPart().getName()));
                                BigDecimal totalPrice = BigDecimal.valueOf(pph.getPrice())
                                                .multiply(BigDecimal.valueOf(pc.getQuantity()))
                                                .setScale(2, RoundingMode.HALF_UP);
                                price += totalPrice.doubleValue();
                        }
                }
                return price;
        }

        @Override
        public SummaryClaimResponse handleSummaryClaim(long serviceCenterId) {
                long countAll = getAllClaims(serviceCenterId);
                long countInProcess = getClaimsByStatus(serviceCenterId, WarrantyClaim.ClaimStatus.PENDING);
                long countSuccess = getClaimsByStatus(serviceCenterId, WarrantyClaim.ClaimStatus.APPROVED);
                double totalPrice = calculateEstimatedCost();

                return SummaryClaimResponse.builder()
                                .total(new SummaryItemResponse(countAll, "All claims"))
                                .pending(new SummaryItemResponse(countInProcess, "Pending claims"))
                                .approved(new SummaryItemResponse(countSuccess, "Approved claims"))
                                .cost(new SummaryItemResponse((long) totalPrice, "Estimated Cost"))
                                .status(true)
                                .build();
        }

}