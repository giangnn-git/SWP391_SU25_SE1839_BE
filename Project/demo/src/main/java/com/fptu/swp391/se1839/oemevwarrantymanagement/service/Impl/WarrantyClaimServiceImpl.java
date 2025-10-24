package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.CampaignVehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.CustomerRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ModelRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartPriceHistoryRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.UserRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehiclePartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrantyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WarrantyClaimServiceImpl implements WarrantyClaimService {

        final UserRepository userRepository;
        final CustomerRepository customerRepository;
        final ModelRepository modelRepository;
        final WarrantyClaimRepository warrantyClaimRepository;
        final PartRepository partRepository;
        final VehicleRepository vehicleRepository;
        final ServiceCenterRepository serviceCenterRepository;
        final PartPriceHistoryRepository partPriceHistoryRepository;
        final CampaignVehicleRepository campaignVehicleRepository;
        final VehiclePartRepository vehiclePartRepository;

        // ================= Dashboard & Summary =================

        public DashboardClaimSummaryResponse handleSummaryClaims(Long serviceCenterId) {
                long count = warrantyClaimRepository.countByServiceCenterId(serviceCenterId);
                long emergencyCount = warrantyClaimRepository.countByServiceCenterIdAndPriority(serviceCenterId,
                                WarrantyClaim.ClaimPriority.URGENT);
                return DashboardClaimSummaryResponse.builder()
                                .count(count)
                                .emegency(emergencyCount)
                                .build();
        }

        long getAllClaims(long serviceCenterId) {
                return warrantyClaimRepository.countByServiceCenterId(serviceCenterId);
        }

        long getClaimsByStatus(long serviceCenterId, WarrantyClaim.ClaimStatus status) {
                return warrantyClaimRepository.countByServiceCenterIdAndStatus(serviceCenterId, status);
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

        // ================= WarrantyClaim Helpers =================

        Vehicle getVehicleByVin(String vin) {
                return vehicleRepository.findByVin(vin)
                                .orElseThrow(() -> new NoSuchElementException("Vehicle not found with vin " + vin));
        }

        ServiceCenter getServiceCenterById(long id) {
                return serviceCenterRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Service center not found with id " + id));
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
                        default -> statuses.clear();
                }
                return statuses;
        }

        double calculateEstimatedCost(WarrantyClaim... claims) {
                double price = 0;
                for (WarrantyClaim wc : claims) {
                        for (PartClaim pc : wc.getPartClaims()) {
                                PartPriceHistory pph = partPriceHistoryRepository
                                                .findCurrentPrice(pc.getPart().getId(), wc.getClaimDate().toLocalDate())
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

        Set<PartClaim> buildPartClaims(Set<PartClaimRequest> requests, WarrantyClaim claim) {
                Set<PartClaim> partClaims = new HashSet<>();
                for (PartClaimRequest pcr : requests) {
                        Part part = partRepository.findById(pcr.getId())
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

        // ================= Create Claim =================

        @Override
        public CreateClaimResponse handleCreateClaim(CreateClaimRequest request, long serviceCenterId,
                        MultipartFile[] attachments, long userId) throws IOException {
                WarrantyClaim.ClaimPriority priorityEnum = resolvePriority(request.getPriority());
                WarrantyClaim warrantyClaim = buildWarrantyClaim(request, serviceCenterId, userId, priorityEnum);
                handleAttachments(warrantyClaim, attachments);
                assignRepairOrder(warrantyClaim);
                assignCampaign(warrantyClaim, request.getVin());
                warrantyClaimRepository.save(warrantyClaim);

                return CreateClaimResponse.builder()
                                .sccuess("success")
                                .message("Registered claim successfully")
                                .build();
        }

        private WarrantyClaim.ClaimPriority resolvePriority(String priority) {
                try {
                        return WarrantyClaim.ClaimPriority.valueOf(priority.toUpperCase());
                } catch (IllegalArgumentException | NullPointerException e) {
                        return WarrantyClaim.ClaimPriority.NORMAL;
                }
        }

        private WarrantyClaim buildWarrantyClaim(CreateClaimRequest request, long serviceCenterId, long userId,
                        WarrantyClaim.ClaimPriority priorityEnum) {
                return WarrantyClaim.builder()
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
        }

        private void handleAttachments(WarrantyClaim warrantyClaim, MultipartFile[] attachments) throws IOException {
                if (attachments == null)
                        return;
                for (MultipartFile file : attachments) {
                        if (!file.isEmpty()) {
                                ClaimAttachment attachment = ClaimAttachment.builder()
                                                .name(file.getOriginalFilename())
                                                .type(file.getContentType())
                                                .imageData(ClaimAttachmentServiceImpl.compressImage(file.getBytes()))
                                                .warrantyClaim(warrantyClaim)
                                                .build();
                                warrantyClaim.getClaimAttachments().add(attachment);
                        }
                }
        }

        private void assignRepairOrder(WarrantyClaim warrantyClaim) {
                RepairOrder ro = RepairOrder.builder()
                                .warrantyClaim(warrantyClaim)
                                .repairDetails(new HashSet<>())
                                .build();
                warrantyClaim.getRepairOrders().add(ro);
        }

        private void assignCampaign(WarrantyClaim warrantyClaim, String vin) {
                CampaignVehicle cv = campaignVehicleRepository.findByVehicleVin(vin)
                                .orElseThrow(() -> new IllegalArgumentException("No campaign found for VIN: " + vin));
                warrantyClaim.setServiceCampaign(cv != null ? cv.getServiceCampaign() : null);
        }

        // ================= Change Status =================

        @Override
        public WarrantyClaimStatusResponse handleChangeStatus(long claimId, WarrantyClaimStatusRequest request,
                        long userId) {
                WarrantyClaim wc = warrantyClaimRepository.findById(claimId)
                                .orElseThrow(() -> new NoSuchElementException("Warranty claim doesn't exist"));

                List<String> duplicatedParts = new ArrayList<>();

                for (PartClaimRequest pcr : request.getParts()) {
                        Part part = partRepository.findById(pcr.getId())
                                        .orElseThrow(() -> new NoSuchElementException(
                                                        "Part not found with id " + pcr.getId()));

                        if (isPartDuplicated(wc.getVehicle(), part)) {
                                duplicatedParts.add("Part '" + part.getName() + "' đã tồn tại trong claim ID: "
                                                + vehiclePartRepository
                                                                .findByVehicleVinAndPartId(wc.getVehicle().getVin(),
                                                                                part.getId())
                                                                .get().getWarrantyClaim().getId());
                                continue;
                        }

                        createVehiclePartAndRepairDetail(wc, part);
                }

                updateClaimStatus(wc, request.getChangeStatus(), request.getReason(), userId);
                warrantyClaimRepository.save(wc);

                if (!duplicatedParts.isEmpty()) {
                        return WarrantyClaimStatusResponse.builder()
                                        .message("Một số part đã tồn tại: " + String.join(", ", duplicatedParts))
                                        .build();
                }

                return WarrantyClaimStatusResponse.builder()
                                .message("Change Status successfully")
                                .build();
        }

        private boolean isPartDuplicated(Vehicle vehicle, Part part) {
                return vehiclePartRepository.findByVehicleVinAndPartId(vehicle.getVin(), part.getId()).isPresent();
        }

        private void createVehiclePartAndRepairDetail(WarrantyClaim wc, Part part) {
                long timestamp = System.currentTimeMillis();
                String serialNumber = "SC" + part.getId() + "-" + timestamp;

                VehiclePart vp = VehiclePart.builder()
                                .part(part)
                                .vehicle(wc.getVehicle())
                                .warrantyClaim(wc)
                                .serialNumber(serialNumber)
                                .installationDate(null)
                                .removalDate(null)
                                .build();
                vehiclePartRepository.save(vp);

                RepairOrder ro = wc.getRepairOrders().iterator().next();
                RepairDetail rd = RepairDetail.builder()
                                .repairOrder(ro)
                                .part(part)
                                .vehiclePart(vp)
                                .description(part.getName())
                                .status(RepairDetail.DetailStatus.PENDING)
                                .build();

                ro.getRepairDetails().add(rd);
                wc.getVehicleParts().add(vp);
        }

        private void updateClaimStatus(WarrantyClaim wc, String newStatusStr, String reason, long userId) {
                if (newStatusStr == null || newStatusStr.isBlank())
                        return;

                try {
                        WarrantyClaim.ClaimStatus newStatus = WarrantyClaim.ClaimStatus
                                        .valueOf(newStatusStr.toUpperCase());

                        if (newStatus == WarrantyClaim.ClaimStatus.REJECTED) {
                                User user = userRepository.findById(userId)
                                                .orElseThrow(() -> new NoSuchElementException("User not found"));
                                wc.setRejectBy(user.getRole().toString());
                                wc.setRejectReason(reason);
                        }

                        wc.setStatus(newStatus);
                } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid status: " + newStatusStr);
                }
        }

        // ================= Claim Detail =================

        @Override
        public ClaimDetailResponse handleGetClaimDetail(long claimId, Long userId) {
                WarrantyClaim wc = warrantyClaimRepository.findById(claimId)
                                .orElseThrow(() -> new NoSuchElementException("Warranty claim doesn't exist"));

                FilterClaimResponse fcr = handleFilterClaim(wc, userId);

                List<DecodeImageReponse> attachments = wc.getClaimAttachments().stream()
                                .map(a -> {
                                        byte[] decompressed = ClaimAttachmentServiceImpl
                                                        .decompressImage(a.getImageData());
                                        String base64 = Base64.getEncoder().encodeToString(decompressed);
                                        String imageDataUrl = "data:" + a.getType() + ";base64," + base64;
                                        return DecodeImageReponse.builder()
                                                        .image(imageDataUrl)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                List<PartQuantityResponse> partQuantity = wc.getPartClaims().stream()
                                .map(pc -> PartQuantityResponse.builder()
                                                .name(pc.getPart().getName())
                                                .quantity(pc.getQuantity())
                                                .category(pc.getPart().getPartCategory())
                                                .description(pc.getPart().getDescription())
                                                .build())
                                .collect(Collectors.toList());

                return ClaimDetailResponse.builder()
                                .fcr(fcr)
                                .images(attachments)
                                .partCLiam(partQuantity)
                                .build();
        }

        // ================= Filter Claim =================

        FilterClaimResponse mapToFilterClaimResponse(WarrantyClaim wc, Long userId) {
                Vehicle vehicle = getVehicleByVin(wc.getVehicle().getVin());
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new NoSuchElementException("User not found"));
                Customer customer = customerRepository.findById(vehicle.getCustomer().getId())
                                .orElseThrow(() -> new RuntimeException("Customer not found"));
                Model model = modelRepository.findById(vehicle.getModel().getId())
                                .orElseThrow(() -> new RuntimeException("Model not found"));

                return FilterClaimResponse.builder()
                                .claimDate(wc.getClaimDate().toLocalDate())
                                .description(wc.getDescription())
                                .price(calculateEstimatedCost(wc))
                                .currentStatus(wc.getStatus().toString())
                                .userName(customer.getName())
                                .productYear(vehicle.getProductYear())
                                .vin(vehicle.getVin())
                                .licensePlate(vehicle.getLicensePlate())
                                .modelName(model.getName())
                                .priority(wc.getPriority().toString())
                                .senderName(userRepository.findById(wc.getUserId())
                                                .orElseThrow(() -> new NoSuchElementException("User not found"))
                                                .getName())
                                .id(wc.getId())
                                .milege(wc.getMileage())
                                .availableStatuses(handleGetAllStatus(wc, user.getRole()))
                                .build();
        }

        List<FilterClaimResponse> handleFilterClaimList(List<WarrantyClaim> wcList, Long userId) {
                return wcList.stream()
                                .map(wc -> mapToFilterClaimResponse(wc, userId))
                                .sorted(Comparator.comparing(FilterClaimResponse::getClaimDate).reversed())
                                .collect(Collectors.toList());
        }

        FilterClaimResponse handleFilterClaim(WarrantyClaim warrantyClaim, Long userId) {
                return mapToFilterClaimResponse(warrantyClaim, userId);
        }

        @Override
        public ClaimDashboardResponse handleClaimDashboard(long serviceCenterId, FilterRequest request, long userId) {
                List<WarrantyClaim> wcList = new ArrayList<>();
                List<FilterClaimResponse> fcrList = new ArrayList<>();
                SummaryClaimResponse scr = new SummaryClaimResponse();
                ServiceCenter sc = getServiceCenterById(serviceCenterId);
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new NoSuchElementException("User not found"));
                WarrantyClaim.ClaimStatus statusEnum = null; // TODO: xử lý từ request nếu cần

                // --- Lấy danh sách claim ---
                wcList = fetchClaimsForDashboard(sc.getId(), user, request, statusEnum);
                fcrList = handleFilterClaimList(wcList, userId);
                scr = handleSummaryClaim(sc.getId(), wcList);

                return ClaimDashboardResponse.builder()
                                .fcr(fcrList)
                                .scr(scr)
                                .build();
        }

        private List<WarrantyClaim> fetchClaimsForDashboard(long serviceCenterId, User user, FilterRequest request,
                        WarrantyClaim.ClaimStatus statusEnum) {
                // TODO: copy logic lọc claims từ code cũ, đảm bảo SRP
                return warrantyClaimRepository.findByServiceCenterId(serviceCenterId); // placeholder
        }
}