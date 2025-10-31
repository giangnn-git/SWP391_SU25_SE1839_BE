package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.*;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.*;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.*;
import com.fptu.swp391.se1839.oemevwarrantymanagement.event.EntityCreatedEvent;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.*;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.WarrantyClaimService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

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
        final PartClaimRepository partClaimRepository;
        final SCExpenseReposiotry scExpenseReposiotry;
        final RepairDetailRepository repairDetailRepository;
        final RepairStepRepository repairStepRepository;
        final ApplicationEventPublisher applicationEventPublisher;

        // ================= Dashboard & Summary =================

        public DashboardClaimSummaryResponse handleSummaryClaims(Long serviceCenterId) {
                long count = warrantyClaimRepository.countByServiceCenterId(serviceCenterId);
                int emergencyCount = warrantyClaimRepository.countByServiceCenterIdAndPriority(serviceCenterId,
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
                double totalEstimatedCost = warrantyClaims.stream()
                                .mapToDouble(this::calculateEstimatedCost)
                                .sum();

                return SummaryClaimResponse.builder()
                                .total(new SummaryItemResponse(countAll, "All claims"))
                                .pending(new SummaryItemResponse(countInProcess, "Pending claims"))
                                .approved(new SummaryItemResponse(countSuccess, "Approved claims"))
                                .cost(new SummaryItemResponse(totalEstimatedCost, "Estimated Cost"))
                                .status(true)
                                .build();
        }

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
                                if (actor == User.Role.SC_STAFF || actor == User.Role.ADMIN) {
                                        statuses.add("PENDING");
                                        statuses.add("REJECTED");
                                }
                        }
                        case REJECTED -> {
                                if (actor == User.Role.TECHNICIAN || actor == User.Role.ADMIN) {
                                        statuses.add("DRAFT");
                                }
                        }
                        case PENDING -> {
                                if (actor == User.Role.EVM_STAFF || actor == User.Role.ADMIN) {
                                        statuses.add("APPROVED");
                                        statuses.add("REJECTED");
                                }
                        }
                        default -> statuses.clear();
                }
                return statuses;
        }

        public double calculateEstimatedCost(WarrantyClaim wc) {
                List<PartClaim> partClaims = partClaimRepository.findByWarrantyClaimId(wc.getId());
                LocalDate claimDate = wc.getClaimDate().toLocalDate();

                return partClaims.stream()
                                .mapToDouble(p -> {
                                        Set<PartPriceHistory> histories = p.getPart().getPartPriceHistories();
                                        double validPrice = histories.stream()
                                                        .filter(h -> !h.getStartDate().isAfter(claimDate))
                                                        .filter(h -> h.getEndDate() == null
                                                                        || !h.getEndDate().isBefore(claimDate))
                                                        .map(PartPriceHistory::getPrice)
                                                        .findFirst()
                                                        .orElseGet(() -> {
                                                                return histories.stream()
                                                                                .max(Comparator.comparing(
                                                                                                PartPriceHistory::getStartDate))
                                                                                .map(PartPriceHistory::getPrice)
                                                                                .orElse(0.0);
                                                        });

                                        return validPrice * p.getQuantity();
                                })
                                .sum();
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

        @Value("${attachment.base-path}")
        String attachmentBasePath;

        private List<String> saveAttachmentsToDocker(WarrantyClaim wc, MultipartFile[] attachments) throws IOException {
                List<String> attachmentBase64 = new ArrayList<>();
                if (attachments == null || attachments.length == 0)
                        return attachmentBase64;

                Path uploadDir = Paths.get(attachmentBasePath, "claims", String.valueOf(wc.getId()));
                Files.createDirectories(uploadDir);

                for (MultipartFile file : attachments) {
                        if (file == null || file.isEmpty())
                                continue;

                        byte[] bytes = file.getBytes(); // đọc 1 lần
                        // sanitize filename
                        String original = file.getOriginalFilename() == null ? "file"
                                        : Paths.get(file.getOriginalFilename()).getFileName().toString();
                        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + "_" + original;
                        Path filePath = uploadDir.resolve(filename);
                        Files.write(filePath, bytes);

                        // chuyển sang base64 (dùng bytes đã đọc)
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        String base64WithPrefix = "data:"
                                        + Optional.ofNullable(file.getContentType()).orElse("application/octet-stream")
                                        + ";base64," + base64;
                        attachmentBase64.add(base64WithPrefix);
                }

                return attachmentBase64;
        }

        @Override
        @Transactional
        public CreateClaimResponse handleCreateClaim(CreateClaimRequest request, long serviceCenterId,
                        MultipartFile[] attachments, long userId) throws IOException {
                WarrantyClaim.ClaimPriority priorityEnum = resolvePriority(request.getPriority());
                WarrantyClaim warrantyClaim = buildWarrantyClaim(request, serviceCenterId, userId, priorityEnum);
                // handleAttachments(warrantyClaim, attachments);
                assignRepairOrder(warrantyClaim);

                if (request.isAgreeRecall()) {
                        assignCampaign(warrantyClaim, request.getVin());
                }

                warrantyClaimRepository.save(warrantyClaim);
                warrantyClaimRepository.flush(); // thêm dòng này!
                applicationEventPublisher.publishEvent(new EntityCreatedEvent<>(this, warrantyClaim));

                List<String> attachmentPaths = saveAttachmentsToDocker(warrantyClaim, attachments);

                return CreateClaimResponse.builder()
                                .sccuess("success")
                                .attachmentBase64(attachmentPaths)
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
                                .repairOrder(null)
                                .vehicleParts(new HashSet<>())
                                .serviceCampaign(null)
                                .priority(priorityEnum)
                                .userId(userId)
                                .build();
        }

        private void assignRepairOrder(WarrantyClaim warrantyClaim) {
                RepairOrder ro = RepairOrder.builder()
                                .warrantyClaim(warrantyClaim)
                                .repairDetails(new HashSet<>())
                                .build();
                warrantyClaim.setRepairOrder(ro);
        }

        private void assignCampaign(WarrantyClaim warrantyClaim, String vin) {
                // Lấy toàn bộ campaign của xe này
                List<CampaignVehicle> campaignVehicles = campaignVehicleRepository.findAllByVehicleVin(vin);
                LocalDate now = LocalDate.now();

                // Lọc ra các campaign đang active (theo thời gian)
                List<CampaignVehicle> activeCampaigns = campaignVehicles.stream()
                                .filter(cv -> {
                                        ServiceCampaign sc = cv.getServiceCampaign();
                                        LocalDate start = sc.getStartDate();
                                        LocalDate end = sc.getEndDate().plusDays(7);
                                        return !now.isBefore(start) && !now.isAfter(end);
                                })
                                .toList();

                // Không có campaign active → bỏ qua
                if (activeCampaigns.isEmpty()) {
                        log.info("Vehicle {} has no active campaign at this time.", vin);
                        return;
                }

                if (activeCampaigns.size() > 1) {
                        String codes = activeCampaigns.stream()
                                        .map(cv -> cv.getServiceCampaign().getCode())
                                        .collect(Collectors.joining(", "));
                        throw new RuntimeException(
                                        "Vehicle " + vin + " belongs to multiple active campaigns: " + codes);
                }

                ServiceCampaign sc = activeCampaigns.get(0).getServiceCampaign();
                warrantyClaim.setServiceCampaign(sc);
                log.info("Assigned campaign {} to claim for VIN {}", sc.getCode(), vin);
        }

        // ================= Change Status =================
        @Override
        public WarrantyClaimStatusResponse handleChangeStatus(
                        long claimId,
                        WarrantyClaimStatusRequest request,
                        long userId) {

                WarrantyClaim wc = warrantyClaimRepository.findById(claimId)
                                .orElseThrow(() -> new NoSuchElementException("Warranty claim doesn't exist"));

                updateClaimStatus(wc, request.getChangeStatus(), request.getReason(), userId);

                warrantyClaimRepository.save(wc);

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new NoSuchElementException("User not found"));

                if (user.getRole() == User.Role.EVM_STAFF || user.getRole() == User.Role.ADMIN) {

                        if (wc.getStatus() == WarrantyClaim.ClaimStatus.APPROVED) {
                                List<RepairDetail> repairDetails = repairDetailRepository.findByRepairOrderId(
                                                wc.getRepairOrder().getId());

                                repairDetails.forEach(rd -> rd.setStatus(RepairDetail.DetailStatus.PENDING));
                                repairDetailRepository.saveAll(repairDetails);
                        }

                        else if (wc.getStatus() == WarrantyClaim.ClaimStatus.REJECTED) {
                                RepairOrder ro = wc.getRepairOrder();
                                if (ro != null) {
                                        List<RepairStep> steps = repairStepRepository.findByRepairOrderId(ro.getId());
                                        if (!steps.isEmpty()) {
                                                repairStepRepository.deleteAll(steps);
                                        }

                                        List<RepairDetail> details = repairDetailRepository
                                                        .findByRepairOrderId(ro.getId());
                                        if (!details.isEmpty()) {
                                                repairDetailRepository.deleteAll(details);
                                        }
                                        wc.setRepairOrder(null);
                                }
                        }
                }

                applicationEventPublisher.publishEvent(new EntityCreatedEvent<>(this, wc));
                return WarrantyClaimStatusResponse.builder()
                                .message("Change Status successfully")
                                .build();
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
                                wc.setDecisionDate(LocalDate.now());
                        }

                        if (newStatus == WarrantyClaim.ClaimStatus.APPROVED) {
                                wc.setDecisionDate(LocalDate.now());

                        }

                        wc.setStatus(newStatus);
                } catch (IllegalArgumentException e) {
                        throw new RuntimeException("Invalid status: " + newStatusStr);
                }
        }

        // ================= Claim Detail =================
        @Override
        public ClaimDetailResponse handleGetClaimDetail(long claimId, Long userId) throws IOException {
                WarrantyClaim wc = warrantyClaimRepository.findById(claimId)
                                .orElseThrow(() -> new NoSuchElementException("Warranty claim doesn't exist"));

                FilterClaimResponse fcr = handleFilterClaim(wc, userId);

                List<DecodeImageReponse> attachments = new ArrayList<>();

                String uploadDir = attachmentBasePath + "/claims/" + wc.getId();
                File dir = new File(uploadDir);
                if (dir.exists() && dir.isDirectory()) {
                        for (File file : Objects.requireNonNull(dir.listFiles())) {
                                byte[] data = Files.readAllBytes(file.toPath());
                                String base64 = Base64.getEncoder().encodeToString(data);
                                String imageDataUrl = "data:" + Files.probeContentType(file.toPath())
                                                + ";base64," + base64;
                                attachments.add(DecodeImageReponse.builder()
                                                .image(imageDataUrl)
                                                .claimAttachmentId(-1L)
                                                .build());
                        }
                }

                List<PartQuantityResponse> partQuantity = wc.getPartClaims().stream()
                                .map(pc -> {
                                        int remainingStock = pc.getPart().getPartInventories().stream()
                                                        .filter(pi -> pi.getServiceCenter().getId().equals(
                                                                        wc.getServiceCenter() != null
                                                                                        ? wc.getServiceCenter().getId()
                                                                                        : -1L))
                                                        .mapToInt(PartInventory::getQuantity)
                                                        .sum();

                                        return PartQuantityResponse.builder()
                                                        .name(pc.getPart().getName())
                                                        .quantity(pc.getQuantity())
                                                        .category(pc.getPart().getPartCategory())
                                                        .description(pc.getPart().getDescription())
                                                        .remainingStock(remainingStock)
                                                        .build();
                                })
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

                CampaignVehicle cv = campaignVehicleRepository.findByVehicleVinAndServiceCampaignId(
                                vehicle.getVin(),
                                wc.getServiceCampaign() != null ? wc.getServiceCampaign().getId() : -1L).orElse(null);

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
                                .modelId(model.getId())
                                .priority(wc.getPriority().toString())
                                .senderName(userRepository.findById(wc.getUserId())
                                                .orElseThrow(() -> new NoSuchElementException("User not found"))
                                                .getName())
                                .id(wc.getId())
                                .milege(wc.getMileage())
                                .availableStatuses(handleGetAllStatus(wc, user.getRole()))
                                .rejectReason(wc.getRejectReason())
                                .statusRecall((cv != null && wc.getServiceCampaign() == null) ? "NOT_AGREED_RECALL"
                                                : (cv != null && wc.getServiceCampaign() != null) ? "AGREED_RECALL"
                                                                : "NO_RECALL")
                                .build();
        }

        List<FilterClaimResponse> handleFilterClaimList(List<WarrantyClaim> wcList, Long userId) {
                return wcList.stream()
                                .map(wc -> mapToFilterClaimResponse(wc, userId))
                                .sorted(Comparator.comparing(FilterClaimResponse::getId).reversed())
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
                WarrantyClaim.ClaimStatus statusEnum = null;

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
                return warrantyClaimRepository.findByServiceCenterId(serviceCenterId);
        }

        public ClaimSummaryResponse calculateRepeatClaimsRateWithComparison(Long serviceCenterId) {
                LocalDate startOfThisMonth = LocalDate.now().withDayOfMonth(1);
                LocalDate endOfThisMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
                LocalDate startOfLastMonth = startOfThisMonth.minusMonths(1);
                LocalDate endOfLastMonth = startOfThisMonth.minusDays(1);

                long totalThisMonth = warrantyClaimRepository.countByServiceCenterIdAndClaimDateBetween(
                                serviceCenterId, startOfThisMonth.atStartOfDay(),
                                endOfThisMonth.plusDays(1).atStartOfDay());
                long repeatThisMonth = warrantyClaimRepository.countRepeatClaimsInRange(
                                serviceCenterId, startOfThisMonth.atStartOfDay(),
                                endOfThisMonth.plusDays(1).atStartOfDay());
                double rateThisMonth = totalThisMonth == 0 ? 0 : (repeatThisMonth * 100.0) / totalThisMonth;

                long totalLastMonth = warrantyClaimRepository.countByServiceCenterIdAndClaimDateBetween(
                                serviceCenterId, startOfLastMonth.atStartOfDay(),
                                endOfLastMonth.plusDays(1).atStartOfDay());
                long repeatLastMonth = warrantyClaimRepository.countRepeatClaimsInRange(
                                serviceCenterId, startOfLastMonth.atStartOfDay(),
                                endOfLastMonth.plusDays(1).atStartOfDay());
                double rateLastMonth = totalLastMonth == 0 ? 0 : (repeatLastMonth * 100.0) / totalLastMonth;

                double changePercent = rateLastMonth == 0 ? 0 : ((rateThisMonth - rateLastMonth) / rateLastMonth) * 100;

                return ClaimSummaryResponse.builder()
                                .currentRate(rateThisMonth)
                                .changePercent(changePercent)
                                .build();
        }

        public List<ModelFailureResponse> calculateFailureAnalysisByModel(Long serviceCenterId) {
                Long totalClaims = warrantyClaimRepository.countByServiceCenterId(serviceCenterId);
                if (totalClaims == 0) {
                        totalClaims = 1L;
                }

                List<Object[]> modelData = warrantyClaimRepository.countServiceCenterIdAndVehicleModel(serviceCenterId);

                List<ModelFailureResponse> modelFailureList = new ArrayList<>();

                for (Object[] row : modelData) {
                        String model = (String) row[0];
                        Long count = (Long) row[1];
                        double percentage = (count * 100.0) / totalClaims;

                        modelFailureList.add(
                                        ModelFailureResponse.builder()
                                                        .model(model)
                                                        .failureRate(Math.round(percentage * 10.0) / 10.0)
                                                        .build());
                }

                return modelFailureList;
        }

        public List<ClaimsByPriorityResponse> calculateClaimsByPriority(Long serviceCenterId) {
                Long totalClaims = warrantyClaimRepository.countByServiceCenterId(serviceCenterId);
                if (totalClaims == 0)
                        totalClaims = 1L;

                List<Object[]> claimsPriority = warrantyClaimRepository.countServiceCenterAndPriority(serviceCenterId);

                List<ClaimsByPriorityResponse> claimsPriorityList = new ArrayList<>();

                for (Object[] row : claimsPriority) {
                        String priority = row[0].toString(); // NORMAL, HIGH, URGENT
                        Long failures = (Long) row[1];

                        claimsPriorityList.add(
                                        ClaimsByPriorityResponse.builder()
                                                        .priority(priority)
                                                        .failures(failures)
                                                        .build());
                }

                return claimsPriorityList;
        }

        private List<String> getLast6Months() {
                YearMonth now = YearMonth.now();
                List<String> last6Months = new ArrayList<>();
                for (int i = 5; i >= 0; i--) {
                        YearMonth ym = now.minusMonths(i);
                        last6Months.add(ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                }
                return last6Months;
        }

        private boolean hasCompletedRepairOrder(WarrantyClaim claim) {
                return claim.getRepairOrder() != null
                                && claim.getRepairOrder().getStatus() == RepairOrder.OrderStatus.COMPLETED;
        }

        long calculateTotalCostForMonth(long serviceCenterId, int year, int month) {
                List<WarrantyClaim> claims = warrantyClaimRepository
                                .findByServiceCenterAndMonth(serviceCenterId, year, month)
                                .stream()
                                .filter(c -> c.getStatus() == WarrantyClaim.ClaimStatus.APPROVED) // chỉ claim được chấp
                                                                                                  // thuận
                                .filter(this::hasCompletedRepairOrder) // chỉ claim có ít nhất 1 RepairOrder hoàn tất
                                .toList();

                double totalCostDouble = claims.stream()
                                .mapToDouble(this::calculateEstimatedCost)
                                .sum();

                return (long) totalCostDouble;
        }

        MonthlyCostSummaryResponse buildMonthlySummary(String monthLabel, long totalClaims, long totalCost,
                        NumberFormat formatter) {
                return MonthlyCostSummaryResponse.builder()
                                .month(monthLabel)
                                .totalClaims(totalClaims)
                                .totalCostFormatted(formatter.format(totalCost))
                                .build();
        }

        List<MonthlyCostSummaryResponse> calculateMonthlySummaries(long serviceCenterId) {
                List<MonthlyCostSummaryResponse> monthlySummaries = new ArrayList<>();
                NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                List<String> last6Months = getLast6Months();

                for (int i = 5; i >= 0; i--) {
                        YearMonth ym = YearMonth.now().minusMonths(i);
                        int month = ym.getMonthValue();
                        int year = ym.getYear();

                        List<WarrantyClaim> claims = warrantyClaimRepository
                                        .findByServiceCenterAndMonth(serviceCenterId, year, month)
                                        .stream()
                                        .filter(c -> c.getStatus() == WarrantyClaim.ClaimStatus.APPROVED)
                                        .filter(this::hasCompletedRepairOrder)
                                        .toList();

                        long totalClaims = claims.size();
                        long totalCost = calculateTotalCostForMonth(serviceCenterId, year, month);

                        monthlySummaries.add(
                                        buildMonthlySummary(last6Months.get(5 - i), totalClaims, totalCost, formatter));
                }

                return monthlySummaries;
        }

        private List<ComponentCostSummaryResponse> calculateCostByComponent(long serviceCenterId) {
                NumberFormat formatter = NumberFormat.getInstance(Locale.US);
                List<ComponentCostSummaryResponse> summaries = new ArrayList<>();

                List<Object[]> componentData = partClaimRepository.countFailuresByComponent(serviceCenterId);

                for (Object[] row : componentData) {
                        Part part = (Part) row[0];
                        long totalFailures = (Long) row[1];

                        List<PartClaim> partClaims = partClaimRepository
                                        .findByServiceCenterAndComponent(serviceCenterId, part.getPartCategory())
                                        .stream()
                                        .filter(pc -> pc.getWarrantyClaim() != null
                                                        && pc.getWarrantyClaim()
                                                                        .getStatus() == WarrantyClaim.ClaimStatus.APPROVED)
                                        .filter(pc -> pc.getWarrantyClaim().getRepairOrder() != null
                                                        && pc.getWarrantyClaim().getRepairOrder()
                                                                        .getStatus() == RepairOrder.OrderStatus.COMPLETED)
                                        .toList();

                        double totalCostDouble = 0;

                        for (PartClaim pc : partClaims) {
                                PartPriceHistory pph = partPriceHistoryRepository
                                                .findCurrentPrice(pc.getPart().getId(),
                                                                pc.getWarrantyClaim().getClaimDate().toLocalDate())
                                                .orElseGet(() -> {
                                                        PartPriceHistory dummy = new PartPriceHistory();
                                                        dummy.setPrice(0.0);
                                                        return dummy;
                                                });

                                totalCostDouble += pph.getPrice() * pc.getQuantity();
                        }

                        long totalCost = (long) totalCostDouble;
                        double avgCost = totalFailures > 0 ? totalCostDouble / totalFailures : 0;

                        summaries.add(ComponentCostSummaryResponse.builder()
                                        .componentName(part.getPartCategory())
                                        .totalFailures(totalFailures)
                                        .totalCostFormatted(formatter.format(totalCost))
                                        .avgCost(avgCost)
                                        .avgCostFormatted(formatter.format(avgCost))
                                        .build());
                }

                return summaries;
        }

        public CostAnalysisResponse handleCalculateClaimCostByMonth(long serviceCenterId) {

                List<MonthlyCostSummaryResponse> monthlySummaries = calculateMonthlySummaries(serviceCenterId);

                long yMax = monthlySummaries.stream()
                                .mapToLong(ms -> Long.parseLong(ms.getTotalCostFormatted().replace(",", "")))
                                .max()
                                .orElse(0);
                yMax = ((yMax / 10000) + 1) * 10000;

                long totalWarrantyCost = monthlySummaries.stream()
                                .mapToLong(ms -> Long.parseLong(ms.getTotalCostFormatted().replace(",", "")))
                                .sum();

                long totalClaimsProcessed = monthlySummaries.stream()
                                .mapToLong(MonthlyCostSummaryResponse::getTotalClaims)
                                .sum();

                double averageCostPerClaim = totalClaimsProcessed > 0
                                ? (double) totalWarrantyCost / totalClaimsProcessed
                                : 0;

                double totalRevenue = scExpenseReposiotry.findTotalRevenueByServiceCenter(serviceCenterId);

                double costOfSalesRatio = totalRevenue > 0
                                ? (totalWarrantyCost / totalRevenue) * 100
                                : 0;

                double averageMonthlyRatio = totalRevenue > 0
                                ? (monthlySummaries.stream()
                                                .mapToDouble(ms -> {
                                                        long totalCost = Long.parseLong(
                                                                        ms.getTotalCostFormatted().replace(",", ""));
                                                        return (totalCost / totalRevenue) * 100;
                                                })
                                                .average()
                                                .orElse(2.5))
                                : 2.5;

                double targetRatio = averageMonthlyRatio * 0.9;

                List<ComponentCostSummaryResponse> componentSummaries = calculateCostByComponent(serviceCenterId);

                return CostAnalysisResponse.builder()
                                .monthlySummaries(monthlySummaries)
                                .componentSummaries(componentSummaries)
                                .yMax(yMax)
                                .totalWarrantyCost(totalWarrantyCost)
                                .averageCostPerClaim(averageCostPerClaim)
                                .totalClaimsProcessed(totalClaimsProcessed)
                                .costOfSalesRatio(costOfSalesRatio)
                                .targetRatio(targetRatio)
                                .build();
        }

        public String handleUpdateClaim(long claimId, CreateClaimRequest request) {
                WarrantyClaim wc = warrantyClaimRepository.findById(claimId)
                                .orElseThrow(() -> new NoSuchElementException("Warranty claim doesn't exist"));
                wc.setDescription(request.getDescription());
                wc.setMileage(request.getMileage());
                wc.setStatus(WarrantyClaim.ClaimStatus.valueOf(request.getStatus()));
                wc.setPriority(WarrantyClaim.ClaimPriority.valueOf(request.getPriority()));
                warrantyClaimRepository.save(wc);
                return "Update Claim succesfully";
        }

        @Override
        public java.util.Map<String, Long> getClaimCountsBreakdown(Long serviceCenterId) {
                java.util.Map<String, Long> map = new java.util.HashMap<>();

                long total = warrantyClaimRepository.countByServiceCenterId(serviceCenterId);
                long draft = warrantyClaimRepository.countByServiceCenterIdAndStatus(serviceCenterId,
                                WarrantyClaim.ClaimStatus.DRAFT);
                long pending = warrantyClaimRepository.countByServiceCenterIdAndStatus(serviceCenterId,
                                WarrantyClaim.ClaimStatus.PENDING);
                long approved = warrantyClaimRepository.countByServiceCenterIdAndStatus(serviceCenterId,
                                WarrantyClaim.ClaimStatus.APPROVED);
                long rejected = warrantyClaimRepository.countByServiceCenterIdAndStatus(serviceCenterId,
                                WarrantyClaim.ClaimStatus.REJECTED);

                java.time.LocalDate today = java.time.LocalDate.now();
                LocalDateTime startToday = today.atStartOfDay();
                LocalDateTime startTomorrow = startToday.plusDays(1);
                long newToday = warrantyClaimRepository.countByServiceCenterIdAndClaimDateBetween(serviceCenterId,
                                startToday, startTomorrow);

                java.time.LocalDate startOfWeekDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDateTime startOfWeek = startOfWeekDate.atStartOfDay();
                LocalDateTime startNextWeek = startOfWeek.plusDays(7);
                long newThisWeek = warrantyClaimRepository.countByServiceCenterIdAndClaimDateBetween(serviceCenterId,
                                startOfWeek, startNextWeek);

                map.put("total", total);
                map.put("draft", draft);
                map.put("pending", pending);
                map.put("approved", approved);
                map.put("rejected", rejected);
                map.put("newToday", newToday);
                map.put("newThisWeek", newThisWeek);

                return map;
        }

}