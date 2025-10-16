package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartClaimRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.WarrantyClaimStatusRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ClaimDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardClaimSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DecodeImageReponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.FilterClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartQuantityResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryClaimResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryItemResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.UploadImageResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.WarrantyClaimStatusResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.CampaignVehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ClaimAttachment;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Customer;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Model;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPriceHistory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCampaign;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim.ClaimPriority;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.*;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarrantyClaimServiceImpl implements WarrantyClaimService {

        final UserRepository userRepository;
        final CustomerRepository customerRepository;
        final ModelRepository modelRepository;
        final WarrantyClaimRepository warrancyClaimRepository;
        final PartRepository partRepository;
        final VehicleRepository vehicleRepository;
        final ServiceCenterRepository serviceCenterRepository;
        final PartPriceHistoryRepository partPriceHistoryRepository;
        final CampaignVehicleRepository campaignVehicleRepository;

        public DashboardClaimSummaryResponse handleSummaryClaims(Long serviceCenterId) {
                long count = this.warrancyClaimRepository.countByServiceCenterId(serviceCenterId);
                long emegencyCount = this.warrancyClaimRepository.countByServiceCenterIdAndPriority(serviceCenterId,
                                WarrantyClaim.ClaimPriority.URGENT);

                return DashboardClaimSummaryResponse.builder()
                                .count(count)
                                .emegency(emegencyCount)
                                .build();
        }

        Vehicle getVehicleByVin(String vin) {
                Vehicle vehicle = this.vehicleRepository.findByVin(vin)
                                .orElseThrow(() -> new NoSuchElementException("Vehicle not found with vin " + vin));
                return vehicle;
        }

        ServiceCenter getServiceCenterById(long id) {
                return this.serviceCenterRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Service center not found with id " + id));
        }

        Set<PartClaim> buildPartClaims(Set<PartClaimRequest> requests, WarrantyClaim claim) {
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

        static void checkDuplicatePartClaim(WarrantyClaim warrantyClaim, List<WarrantyClaim> existingClaims) {
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
                        MultipartFile[] attachments, long userId)
                        throws IOException {

                // --- Xử lý priority ---
                ClaimPriority priorityEnum;
                try {
                        priorityEnum = ClaimPriority.valueOf(request.getPriority().toUpperCase());
                } catch (IllegalArgumentException | NullPointerException e) {
                        priorityEnum = ClaimPriority.NORMAL;
                }

                // --- Tạo đối tượng WarrantyClaim ---
                WarrantyClaim warrantyClaim = WarrantyClaim.builder()
                                .description(request.getDescription())
                                .mileage(request.getMileage())
                                .vehicle(getVehicleByVin(request.getVin()))
                                .serviceCenter(getServiceCenterById(serviceCenterId))
                                .claimAttachments(new ArrayList<>())
                                .repairOrders(new HashSet<>())
                                .vehicleParts(new HashSet<>())
                                .serviceCampaign(null)
                                .priority(priorityEnum)
                                .userId(userId)
                                .build();

                // --- Gán PartClaims ---
                warrantyClaim.setPartClaims(buildPartClaims(request.getPartClaims(), warrantyClaim));

                // --- Kiểm tra trùng lặp ---
                List<WarrantyClaim> existingClaims = warrancyClaimRepository
                                .findByVehicleVinAndClaimDate(
                                                warrantyClaim.getVehicle().getVin(),
                                                warrantyClaim.getClaimDate());
                checkDuplicatePartClaim(warrantyClaim, existingClaims);

                // --- Xử lý file đính kèm ---
                if (attachments != null) {
                        for (MultipartFile file : attachments) {
                                if (!file.isEmpty()) {
                                        ClaimAttachment attachment = ClaimAttachment.builder()
                                                        .name(file.getOriginalFilename())
                                                        .type(file.getContentType())
                                                        .imageData(ClaimAttachmentServiceImpl
                                                                        .compressImage(file.getBytes()))
                                                        .warrantyClaim(warrantyClaim)
                                                        .build();
                                        warrantyClaim.getClaimAttachments().add(attachment);
                                }
                        }
                }

                // --- Tạo RepairOrder ---
                RepairOrder ro = RepairOrder.builder()
                                .warrantyClaim(warrantyClaim)
                                .repairDetails(new HashSet<>())
                                .build();

                // --- Tạo RepairDetail & VehiclePart ---
                for (PartClaim pc : warrantyClaim.getPartClaims()) {
                        long partId = pc.getPart().getId();
                        long timestamp = System.currentTimeMillis();
                        String serialNumber = "SC" + partId + "-" + timestamp;

                        VehiclePart vp = VehiclePart.builder()
                                        .part(pc.getPart())
                                        .vehicle(warrantyClaim.getVehicle())
                                        .warrantyClaim(warrantyClaim)
                                        .serialNumber(serialNumber)
                                        .installationDate(null)
                                        .removalDate(null)
                                        .build();

                        RepairDetail rd = RepairDetail.builder()
                                        .repairOrder(ro)
                                        .part(pc.getPart())
                                        .vehiclePart(vp) // ✅ gắn VehiclePart vào RepairDetail
                                        .description(pc.getPart().getName())
                                        .status(RepairDetail.DetailStatus.PENDING)
                                        .build();

                        ro.getRepairDetails().add(rd);
                        warrantyClaim.getVehicleParts().add(vp);
                }

                // --- Gán ServiceCampaign nếu có ---
                CampaignVehicle cv = campaignVehicleRepository.findByVehicleVin(request.getVin());
                if (cv != null && cv.getServiceCampaign() != null) {
                        warrantyClaim.setServiceCampaign(cv.getServiceCampaign());
                } else {
                        warrantyClaim.setServiceCampaign(null);
                }

                // --- Liên kết RepairOrder với WarrantyClaim ---
                warrantyClaim.getRepairOrders().add(ro);

                // --- Lưu toàn bộ Claim ---
                warrancyClaimRepository.save(warrantyClaim);

                // --- Trả về phản hồi ---
                return CreateClaimResponse.builder()
                                .sccuess("success")
                                .message("Registered claim successfully")
                                .build();
        }

        long getAllClaims(long serviceCenterId) {
                return warrancyClaimRepository.countByServiceCenterId(serviceCenterId);
        }

        long getClaimsByStatus(long serviceCenterId, WarrantyClaim.ClaimStatus status) {
                return warrancyClaimRepository.countByServiceCenterIdAndStatus(serviceCenterId, status);
        }

        Set<String> handleGetAllStatus(WarrantyClaim claim, User.Role actor) {
                Set<String> statuses = new LinkedHashSet<>();

                switch (claim.getStatus()) {
                        case DRAFT -> {
                                if (actor == User.Role.SC_STAFF) {
                                        statuses.add("PENDING");
                                        statuses.add("REJECTED");
                                }
                        }
                        case REJECTED -> {
                                if (actor == User.Role.TECHNICIAN) {
                                        statuses.add("DRAFT");
                                }
                        }
                        case PENDING -> {
                                if (actor == User.Role.EVM_STAFF) {
                                        statuses.add("APPROVED");
                                        statuses.add("REJECTED");
                                }
                        }
                        default -> {
                                // APPROVED hoặc DONE -> không cho đổi
                                statuses.clear();
                        }
                }

                return statuses;
        }

        double calculateEstimatedCost(WarrantyClaim... claims) {
                double price = 0;
                for (WarrantyClaim wc : claims) {
                        for (PartClaim pc : wc.getPartClaims()) {
                                PartPriceHistory pph = partPriceHistoryRepository
                                                .findCurrentPrice(pc.getPart().getId(), wc.getClaimDate())
                                                .orElseGet(() -> {
                                                        log.warn("⚠️ No price found for part: {} (Claim ID: {})",
                                                                        pc.getPart().getName(), wc.getId());
                                                        PartPriceHistory dummy = new PartPriceHistory();
                                                        dummy.setPrice(0.0);
                                                        return dummy;
                                                });
                                BigDecimal totalPrice = BigDecimal.valueOf(pph.getPrice())
                                                .multiply(BigDecimal.valueOf(pc.getQuantity()))
                                                .setScale(2, RoundingMode.HALF_UP);
                                price += totalPrice.doubleValue();
                        }
                }
                return price;
        }

        SummaryClaimResponse handleSummaryClaim(long serviceCenterId, List<WarrantyClaim> warrantyClaims) {
                long countAll = getAllClaims(serviceCenterId);
                long countInProcess = getClaimsByStatus(serviceCenterId, WarrantyClaim.ClaimStatus.PENDING);
                long countSuccess = getClaimsByStatus(serviceCenterId, WarrantyClaim.ClaimStatus.APPROVED);
                double totalPrice = calculateEstimatedCost(warrantyClaims.toArray(new WarrantyClaim[0]));

                return SummaryClaimResponse.builder()
                                .total(new SummaryItemResponse(countAll, "All claims"))
                                .pending(new SummaryItemResponse(countInProcess, "Pending claims"))
                                .approved(new SummaryItemResponse(countSuccess, "Approved claims"))
                                .cost(new SummaryItemResponse((long) totalPrice, "Estimated Cost"))
                                .status(true)
                                .build();
        }

        FilterClaimResponse mapToFilterClaimResponse(WarrantyClaim wc, Long userId) {
                Vehicle vehicle = getVehicleByVin(wc.getVehicle().getVin());
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new NoSuchElementException("User not found"));
                Customer customer = customerRepository.findById(vehicle.getCustomer().getId())
                                .orElseThrow(() -> new RuntimeException("Customer not found"));
                Model model = modelRepository.findById(vehicle.getModel().getId())
                                .orElseThrow(() -> new RuntimeException("Model not found"));

                return FilterClaimResponse.builder()
                                .claimDate(wc.getClaimDate())
                                .description(wc.getDescription())
                                .price(calculateEstimatedCost(wc))
                                .currentStatus(wc.getStatus().toString())
                                .userName(customer.getName())
                                .productYear(vehicle.getProductYear())
                                .vin(vehicle.getVin())
                                .modelName(model.getName())
                                .priority(wc.getPriority().toString())
                                .senderName(user.getName())
                                .id(wc.getId())
                                .milege(wc.getMileage())
                                .availableStatuses(handleGetAllStatus(wc, user.getRole()))
                                .build();
        }

        List<FilterClaimResponse> handleFilterClaimList(List<WarrantyClaim> wcList, Long userId) {
                return wcList.stream()
                                .map(wc -> mapToFilterClaimResponse(wc, userId))
                                .collect(Collectors.toList());
        }

        FilterClaimResponse handleFilterClaim(WarrantyClaim warrantyClaim, Long userId) {
                return mapToFilterClaimResponse(warrantyClaim, userId);
        }

        @Override
        public ClaimDashboardResponse handleClaimDashboard(long serviceCenterId, FilterRequest request,
                        long userId) {
                List<WarrantyClaim> wcList = new ArrayList<>();
                List<FilterClaimResponse> fcrList = new ArrayList<>();
                SummaryClaimResponse scr = new SummaryClaimResponse();
                ServiceCenter sc = getServiceCenterById(serviceCenterId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new NoSuchElementException("User not found"));
                WarrantyClaim.ClaimStatus statusEnum = null;
                if ((request.getKeyword() == null || request.getKeyword().isEmpty()) && statusEnum == null) {
                        if (user.getRole() == User.Role.TECHNICIAN) {
                                wcList = this.warrancyClaimRepository.findByServiceCenterIdAndUserId(sc.getId(),
                                                user.getId());
                        } else {
                                wcList = this.warrancyClaimRepository.findByServiceCenterId(sc.getId());
                        }
                        fcrList = handleFilterClaimList(wcList, userId);
                        scr = handleSummaryClaim(sc.getId(), wcList);

                } else if ((request.getKeyword() == null || request.getKeyword().isEmpty()) && statusEnum != null) {
                        if (user.getRole() == User.Role.TECHNICIAN) {
                                wcList = this.warrancyClaimRepository.findByServiceCenterIdAndStatusAndUserId(
                                                sc.getId(),
                                                statusEnum, userId);
                        } else {
                                wcList = this.warrancyClaimRepository.findByServiceCenterIdAndStatus(sc.getId(),
                                                statusEnum);
                        }
                        fcrList = handleFilterClaimList(wcList, userId);
                        scr = handleSummaryClaim(sc.getId(), wcList);

                } else if (request.getKeyword() != null && statusEnum == null) {
                        if (user.getRole() == User.Role.TECHNICIAN) {
                                wcList = this.warrancyClaimRepository.findByServiceCenterIdAndVehicleVinAndUserId(
                                                sc.getId(),
                                                request.getKeyword(), userId);
                        } else {
                                wcList = this.warrancyClaimRepository.findByServiceCenterIdAndVehicleVin(sc.getId(),
                                                request.getKeyword());
                        }
                        if (wcList == null || wcList.isEmpty()) {
                                if (user.getRole() == User.Role.TECHNICIAN) {
                                        wcList = this.warrancyClaimRepository.findByCustomerNameAndUserId(sc.getId(),
                                                        request.getKeyword(), userId);
                                } else {
                                        wcList = this.warrancyClaimRepository.findByCustomerName(sc.getId(),
                                                        request.getKeyword());
                                }
                        }
                        fcrList = handleFilterClaimList(wcList, userId);
                        scr = handleSummaryClaim(sc.getId(), wcList);

                } else {
                        if (user.getRole() == User.Role.TECHNICIAN) {
                                wcList = this.warrancyClaimRepository
                                                .findByServiceCenterIdAndVehicleVinAndStatusAndUserId(
                                                                sc.getId(), request.getKeyword(), statusEnum, userId);
                        } else {
                                wcList = this.warrancyClaimRepository.findByServiceCenterIdAndVehicleVinAndStatus(
                                                sc.getId(), request.getKeyword(), statusEnum);
                        }
                        if (wcList == null || wcList.isEmpty()) {
                                if (user.getRole() == User.Role.TECHNICIAN) {
                                        wcList = this.warrancyClaimRepository
                                                        .findByServiceCenterIdAndCustomerNameAndStatusAndUserId(
                                                                        sc.getId(), request.getKeyword(), statusEnum,
                                                                        userId);
                                } else {
                                        wcList = this.warrancyClaimRepository
                                                        .findByServiceCenterIdAndCustomerNameAndStatus(
                                                                        sc.getId(), request.getKeyword(), statusEnum);
                                }
                        }
                        fcrList = handleFilterClaimList(wcList, userId);
                        scr = handleSummaryClaim(sc.getId(), wcList);
                }

                return ClaimDashboardResponse.builder()
                                .fcr(fcrList)
                                .scr(scr)
                                .build();
        }

        @Override
        public WarrantyClaimStatusResponse handleChangeStatus(long claimId, WarrantyClaimStatusRequest request,
                        long userId) {
                WarrantyClaim wc = warrancyClaimRepository.findById(claimId)
                                .orElseThrow(() -> new NoSuchElementException("Warranty claim doesn't exist"));

                if (request.getChangeStatus() == null || request.getChangeStatus().isBlank()) {
                        return WarrantyClaimStatusResponse.builder()
                                        .message("Change status must not be null or empty")
                                        .build();
                }

                try {
                        WarrantyClaim.ClaimStatus newStatus = WarrantyClaim.ClaimStatus
                                        .valueOf(request.getChangeStatus().toUpperCase());

                        if (newStatus == WarrantyClaim.ClaimStatus.REJECTED) {
                                User user = userRepository.findById(userId)
                                                .orElseThrow(() -> new NoSuchElementException("User not found"));
                                wc.setRejectBy(user.getRole().toString());
                                wc.setRejectReason(request.getReason());
                        }

                        wc.setStatus(newStatus);
                        warrancyClaimRepository.save(wc);

                        return WarrantyClaimStatusResponse.builder()
                                        .message("Change Status successfully")
                                        .build();

                } catch (IllegalArgumentException e) {
                        return WarrantyClaimStatusResponse.builder()
                                        .message("Invalid status: " + request.getChangeStatus())
                                        .build();
                }
        }

        @Override
        public ClaimDetailResponse handleGetClaimDetail(long claimId, Long userId) {
                WarrantyClaim wc = warrancyClaimRepository.findById(claimId)
                                .orElseThrow(() -> new NoSuchElementException("Warranty claim doesn't exist"));

                FilterClaimResponse fcr = handleFilterClaim(wc, userId);

                List<DecodeImageReponse> attachments = wc.getClaimAttachments().stream()
                                .map(a -> {
                                        byte[] decompressed = ClaimAttachmentServiceImpl
                                                        .decompressImage(a.getImageData());
                                        String base64 = Base64.getEncoder().encodeToString(decompressed);
                                        String imageDataUrl = "data:" + a.getType() + ";base64," + base64; // thêm MIME
                                                                                                           // type
                                        return DecodeImageReponse.builder()
                                                        .image(imageDataUrl)
                                                        .build();
                                })
                                .collect(Collectors.toList());
                List<PartQuantityResponse> partQuantity = new ArrayList<>();
                for (PartClaim pc : wc.getPartClaims()) {
                        PartQuantityResponse part = PartQuantityResponse.builder()
                                        .name(pc.getPart().getName())
                                        .quantity(pc.getQuantity())
                                        .category(pc.getPart().getPartCategory())
                                        .description(pc.getPart().getDescription())
                                        .build();
                        partQuantity.add(part);
                }
                return ClaimDetailResponse.builder()
                                .fcr(fcr)
                                .images(attachments)
                                .partCLiam(partQuantity)
                                .build();
        }

}