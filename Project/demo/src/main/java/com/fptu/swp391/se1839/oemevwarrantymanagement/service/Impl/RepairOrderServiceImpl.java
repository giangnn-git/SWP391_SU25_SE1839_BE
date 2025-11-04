package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChooseTechnicalRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.EmailDetailsRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.FilterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChooseTechnicalResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DashboardOrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.FilterOrderResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetTechnicalsResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDashboardResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.OrderSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryItemResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.SummaryOrderResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.TechnicalsResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Customer;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Model;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPriceHistory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairStep;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.SCExpense;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.event.EntityUpdatedEvent;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ModelRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairOrderRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairStepRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.SCExpenseReposiotry;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.UserRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.WarrantyClaimRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.EmailService;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.RepairOrderService;

import jakarta.persistence.PersistenceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RepairOrderServiceImpl implements RepairOrderService {

    final RepairOrderRepository repairOrderRepository;
    final VehicleRepository vehicleRepository;
    final ServiceCenterRepository serviceCenterRepository;
    final WarrantyClaimRepository warrantyClaimRepository;
    final ModelRepository modelRepository;
    final RepairStepRepository repairStepRepository;
    final UserRepository userRepository;
    final RepairDetailRepository repairDetailRepository;
    final SCExpenseReposiotry scExpenseReposiotry;
    final EmailService emailService;
    final ApplicationEventPublisher applicationEventPublisher;

    Vehicle getVehicleByVin(String vin) {
        Vehicle vehicle = this.vehicleRepository.findByVin(vin)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found with vin " + vin));
        return vehicle;
    }

    WarrantyClaim getWarrantyClaimId(long id) {
        return this.warrantyClaimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Warranty claim not found with id " + id));
    }

    ServiceCenter getServiceCenterById(long id) {
        return this.serviceCenterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Service center not found with id " + id));
    }

    SummaryItemResponse calculateSummary(long current, long previous) {
        long percentage;
        String message;

        if (previous == 0) {
            percentage = current > 0 ? 100 : 0;
            message = current > 0 ? "Increase" : "No Change";
        } else if (current > previous) {
            percentage = (current - previous) * 100 / previous;
            message = "Increase";
        } else {
            percentage = (previous - current) * 100 / previous;
            message = "Decrease";
        }

        return new SummaryItemResponse(percentage, message);
    }

    SummaryItemResponse calculateWeekSummary(long serviceCenterId) {
        LocalDate currentStart = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate currentEnd = LocalDate.now().with(DayOfWeek.SUNDAY);

        long currentWeek = repairOrderRepository.countRepairFlWeek(serviceCenterId, currentStart, currentEnd);

        LocalDate prevStart = currentStart.minusWeeks(1);
        LocalDate prevEnd = currentEnd.minusWeeks(1);

        long previousWeek = repairOrderRepository.countRepairFlWeek(serviceCenterId, prevStart, prevEnd);

        return calculateSummary(currentWeek, previousWeek);
    }

    SummaryItemResponse calculateMonthSummary(long serviceCenterId) {
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        long currentMonth = repairOrderRepository.countRepairFlStatusAndMonth(
                serviceCenterId, RepairOrder.OrderStatus.COMPLETED, startMonth, endMonth);

        LocalDate prevStart = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate prevEnd = LocalDate.now().minusMonths(1)
                .withDayOfMonth(LocalDate.now().minusMonths(1).lengthOfMonth());

        long previousMonth = repairOrderRepository.countRepairFlStatusAndMonth(
                serviceCenterId, RepairOrder.OrderStatus.COMPLETED, prevStart, prevEnd);

        return calculateSummary(currentMonth, previousMonth);
    }

    @Override
    public DashboardOrderSummaryResponse findSunSummaryOrder(long serviceCenterId) {
        boolean status = true;
        long countOrderOneWeek = 0;
        SummaryItemResponse weekResult = new SummaryItemResponse(0, "No Data");
        SummaryItemResponse monthResult = new SummaryItemResponse(0, "No Data");

        try {
            countOrderOneWeek = this.repairOrderRepository.countRepairFlStatus(
                    serviceCenterId, RepairOrder.OrderStatus.IN_PROGRESS);

            weekResult = calculateWeekSummary(serviceCenterId);
            monthResult = calculateMonthSummary(serviceCenterId);

        } catch (DataAccessException | PersistenceException e) {
            log.error("Error while calculating dashboard summary", e);
            status = false;
        }

        return DashboardOrderSummaryResponse.builder()
                .countOrderInOneWeek(countOrderOneWeek)
                .differenceOneWeek(weekResult.getPercentage())
                .completeOneMonth(monthResult.getPercentage())
                .differenceOneMonth(monthResult.getPercentage())
                .status(status)
                .build();
    }

    long getOrderByStatus(long serviceCenterId, RepairOrder.OrderStatus status) {
        return repairOrderRepository.countByServiceCenterIdAndStatus(serviceCenterId, status);
    }

    double countAvgComplete(List<RepairOrder> roList) {
        List<RepairOrder> repairOrders = new ArrayList<>();
        for (int i = 0; i < roList.size(); i++) {
            if (roList.get(i).getStatus() == RepairOrder.OrderStatus.COMPLETED) {
                repairOrders.add(roList.get(i));
            }
        }

        long totalDays = 0;
        int count = 0;

        for (RepairOrder ro : repairOrders) {
            if (ro.getEndDate() != null && ro.getStartDate() != null) {
                long daysBetween = Duration.between(ro.getStartDate(), ro.getEndDate()).toDays();
                totalDays += daysBetween;
                count++;
            }
        }

        return count > 0 ? (double) totalDays / count : 0;
    }

    public double calculateProgress(Long repairOrderId) {
        List<RepairStep> steps = repairStepRepository.findByRepairOrderId(repairOrderId);

        if (steps.isEmpty())
            return 0;

        long total = steps.size();
        long completed = steps.stream()
                .filter(d -> d.getStatus() == RepairStep.StepStatus.COMPLETED
                        || d.getStatus() == RepairStep.StepStatus.CANCELLED)
                .count();

        return (completed * 100.0) / total;
    }

    SummaryOrderResponse handleSummary(long serviceCenterId, List<RepairOrder> roList) {
        long countInProcess = getOrderByStatus(serviceCenterId, RepairOrder.OrderStatus.IN_PROGRESS);
        long countInWaiting = getOrderByStatus(serviceCenterId, RepairOrder.OrderStatus.WAITING);
        long countInComplete = getOrderByStatus(serviceCenterId, RepairOrder.OrderStatus.COMPLETED);
        double avgDays = countAvgComplete(roList);

        return SummaryOrderResponse.builder()
                .active(new SummaryItemResponse(countInProcess, "In-process order"))
                .complete(new SummaryItemResponse(countInComplete, "Complete order"))
                .waitting(new SummaryItemResponse(countInWaiting, "Waitting order"))
                .avgComplete(new SummaryItemResponse((long) avgDays, "Avg complete order"))
                .status(true)
                .build();
    }

    public List<FilterOrderResponse> handleFilterOrder(List<RepairOrder> roList) {

        List<FilterOrderResponse> forList = new ArrayList<>();

        for (RepairOrder ro : roList) {
            double progress = calculateProgress(ro.getId());

            if (progress == 0) {
                ro.setStatus(RepairOrder.OrderStatus.WAITING);
            } else if (progress < 100) {
                ro.setStatus(RepairOrder.OrderStatus.IN_PROGRESS);
            } else {
                ro.setStatus(RepairOrder.OrderStatus.COMPLETED);

                if (ro.getEndDate() == null) {
                    ro.setEndDate(LocalDateTime.now());
                }

                createExpensesIfOrderCompleted(ro);
            }

            repairOrderRepository.save(ro);

            applicationEventPublisher.publishEvent(new EntityUpdatedEvent<>(this, ro));

            String technicalName = ro.getTechnical() != null ? ro.getTechnical().getName() : "Unknown";

            WarrantyClaim claim = getWarrantyClaimId(ro.getWarrantyClaim().getId());
            Vehicle vehicle = getVehicleByVin(claim.getVehicle().getVin());
            Model model = modelRepository.findById(vehicle.getModel().getId())
                    .orElseThrow(() -> new RuntimeException("Model not found"));

            FilterOrderResponse response = FilterOrderResponse.builder()
                    .repairOrderId(ro.getId())
                    .claimId(claim.getId())
                    .claimStatus(claim.getStatus().toString())
                    .percentInProcess(progress)
                    .techinal(technicalName)
                    .prodcutYear(vehicle.getProductYear())
                    .vin(vehicle.getVin())
                    .licensePlate(vehicle.getLicensePlate())
                    .modelName(model.getName())
                    .orderDate(ro.getStartDate()) // sử dụng startDate để sắp xếp
                    .build();

            forList.add(response);
        }

        forList.sort(Comparator.comparing(FilterOrderResponse::getRepairOrderId).reversed());

        return forList;
    }

    @Override
    public OrderDashboardResponse handleOrderDashboard(long serviceCenterId, FilterRequest request, Long userId) {
        List<RepairOrder> roList = new ArrayList<>();
        List<FilterOrderResponse> responseList = new ArrayList<>();
        SummaryOrderResponse sor = new SummaryOrderResponse();
        ServiceCenter sc = getServiceCenterById(serviceCenterId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        RepairOrder.OrderStatus statusEnum = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            statusEnum = RepairOrder.OrderStatus.valueOf(request.getStatus().toUpperCase());
        }

        if (request.getKeyword() == null && request.getStatus() == null) {
            if (user.getRole() == User.Role.TECHNICIAN) {
                roList = repairOrderRepository.findByServiceCenterIdAndUserId(sc.getId(), userId);
            } else {
                roList = repairOrderRepository.findByServiceCenterId(sc.getId());
            }
        } else if (request.getKeyword() == null && request.getStatus() != null) {
            if (user.getRole() == User.Role.TECHNICIAN) {
                roList = repairOrderRepository.findByServiceCenterIdAndStatusAndUserId(sc.getId(), statusEnum, userId);
            } else {
                roList = repairOrderRepository.findByServiceCenterIdAndStatus(sc.getId(), statusEnum);
            }
        } else if (request.getKeyword() != null && request.getStatus() == null) {
            if (user.getRole() == User.Role.TECHNICIAN) {
                roList = repairOrderRepository.findByServiceCenterIdAndVehicleVinAndUserId(sc.getId(),
                        request.getKeyword(), userId);
            } else {
                roList = repairOrderRepository.findByServiceCenterIdAndVehicleVin(sc.getId(), request.getKeyword());
            }
            if (roList == null || roList.isEmpty()) {
                if (user.getRole() == User.Role.TECHNICIAN) {
                    roList = repairOrderRepository.findByCustomerNameAndUserId(sc.getId(),
                            request.getKeyword(), userId);
                } else {
                    roList = repairOrderRepository.findByCustomerName(sc.getId(), request.getKeyword());
                }
            }
        } else {
            if (user.getRole() == User.Role.TECHNICIAN) {
                roList = repairOrderRepository.findByServiceCenterIdAndVehicleVinAndStatusAndUserId(sc.getId(),
                        request.getKeyword(), statusEnum, userId);
            } else {
                roList = repairOrderRepository.findByServiceCenterIdAndVehicleVinAndStatus(sc.getId(),
                        request.getKeyword(),
                        statusEnum);
            }
            if (roList == null || roList.isEmpty()) {
                if (user.getRole() == User.Role.TECHNICIAN) {
                    roList = repairOrderRepository.findByServiceCenterIdAndCustomerNameAndStatusAndUserId(sc.getId(),
                            request.getKeyword(), statusEnum, userId);
                } else {
                    roList = repairOrderRepository.findByServiceCenterIdAndCustomerNameAndStatus(sc.getId(),
                            request.getKeyword(), statusEnum);
                }
            }
        }

        responseList = handleFilterOrder(roList);
        sor = handleSummary(sc.getId(), roList);

        return OrderDashboardResponse.builder()
                .fors(responseList)
                .sor(sor)
                .build();
    }

    @Override
    public ChooseTechnicalResponse handleChooseTechnical(long repairOrderId, ChooseTechnicalRequest request) {
        // Lấy RepairOrder theo id
        RepairOrder repairOrder = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new NoSuchElementException("Repair order not found: " + repairOrderId));

        // Lấy WarrantyClaim liên kết với RepairOrder
        WarrantyClaim claim = repairOrder.getWarrantyClaim();
        if (claim == null) {
            throw new IllegalStateException("Repair order has no associated warranty claim");
        }

        // ❗ Kiểm tra trạng thái claim
        if (claim.getStatus() != WarrantyClaim.ClaimStatus.APPROVED) {
            throw new IllegalStateException("Cannot assign technician: warranty claim is not APPROVED");
        }

        // Lấy kỹ thuật viên theo tên
        User technical = userRepository.findByName(request.getTechnicalName());
        if (technical == null) {
            throw new NoSuchElementException("Technician not found: " + request.getTechnicalName());
        }

        // Gán thông tin kỹ thuật và các trường khác
        repairOrder.setTechnical(technical);
        repairOrder.setStatus(RepairOrder.OrderStatus.PENDING);

        repairOrder.getSteps().forEach(step -> {
            step.setAssignedTechnician(technical.getName());
            if (step.getStatus() == RepairStep.StepStatus.WAITING) {
                step.setStatus(RepairStep.StepStatus.PENDING);
            }
        });

        // Lưu RepairOrder
        repairOrderRepository.save(repairOrder);
        applicationEventPublisher.publishEvent(new EntityUpdatedEvent<>(this, repairOrder));

        // Trả về phản hồi
        return ChooseTechnicalResponse.builder()
                .message("Technician " + technical.getName() + " assigned to repair order " + repairOrderId)
                .status(true)
                .build();
    }

    FilterOrderResponse handleFilterOrder(long orderId) {
        RepairOrder ro = this.repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Not find repair order"));

        String technicalName = (ro.getTechnical() != null) ? ro.getTechnical().getName() : null;

        return FilterOrderResponse.builder()
                .repairOrderId(orderId)
                .claimId(ro.getWarrantyClaim().getId())
                .claimStatus(ro.getWarrantyClaim().getStatus().toString())
                .prodcutYear(ro.getWarrantyClaim().getVehicle().getProductYear())
                .modelName(ro.getWarrantyClaim().getVehicle().getModel().getName())
                .vin(ro.getWarrantyClaim().getVehicle().getVin())
                .licensePlate(ro.getWarrantyClaim().getVehicle().getLicensePlate())
                .techinal(technicalName)
                .percentInProcess(calculateProgress(orderId))
                .build();
    }

    GetTechnicalsResponse handleTechnicalStatus(long serviceCenterId, long orderId) {
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order is not found: " + orderId));

        if (order.getTechnical() != null) {
            long countJobs = repairOrderRepository.countByTechnicalIdAndStatusIn(
                    order.getTechnical().getId(),
                    Arrays.asList(RepairOrder.OrderStatus.WAITING, RepairOrder.OrderStatus.PENDING,
                            RepairOrder.OrderStatus.IN_PROGRESS));

            String message = countJobs > 0
                    ? "Busy, having " + countJobs + " task(s) processing"
                    : "Available";

            TechnicalsResponse technicians = TechnicalsResponse.builder()
                    .id(order.getTechnical().getId())
                    .name(order.getTechnical().getName())
                    .countJob(countJobs)
                    .message(message)
                    .build();
            return GetTechnicalsResponse.builder()
                    .technicians(Collections.singletonList(technicians))
                    .build();
        }

        // Lấy danh sách tất cả kỹ thuật viên ở trung tâm
        List<User> technicians = userRepository
                .findByWorkStatusInAndServiceCenterIdAndRole(
                        Arrays.asList(User.WorkStatus.AVAILABLE, User.WorkStatus.BUSY),
                        serviceCenterId,
                        User.Role.TECHNICIAN);

        List<TechnicalsResponse> result = new ArrayList<>();

        for (User tech : technicians) {
            // Bỏ qua nếu kỹ thuật viên nghỉ hôm nay
            boolean hasLeave = userRepository.existsByTechnicianIdAndDate(tech.getId(), LocalDate.now());
            if (hasLeave)
                continue;

            // Đếm số công việc đang chờ hoặc đang làm
            long countJobs = repairOrderRepository.countByTechnicalIdAndStatusIn(
                    tech.getId(),
                    Arrays.asList(RepairOrder.OrderStatus.WAITING, RepairOrder.OrderStatus.PENDING,
                            RepairOrder.OrderStatus.IN_PROGRESS));

            result.add(TechnicalsResponse.builder()
                    .id(tech.getId())
                    .name(tech.getName())
                    .countJob(countJobs)
                    .message("Đang hoạt động")
                    .build());
        }

        return GetTechnicalsResponse.builder()
                .technicians(result)
                .build();
    }

    public OrderDetailResponse handleGetDetailOrder(long serviceCenterId, long orderId) {
        return OrderDetailResponse.builder()
                .filterOrderResponse(handleFilterOrder(orderId))
                .getTechnicalsResponse(handleTechnicalStatus(serviceCenterId, orderId))
                .build();
    }

    private void createExpensesIfOrderCompleted(RepairOrder order) {
        if (order.getStatus() != RepairOrder.OrderStatus.COMPLETED)
            return;

        List<RepairDetail> replacedDetails = repairDetailRepository.findByRepairOrderId(order.getId())
                .stream()
                .filter(d -> d.getStatus() == RepairDetail.DetailStatus.REPLACED)
                .toList();

        for (RepairDetail d : replacedDetails) {
            Double price = d.getPart().getPartPriceHistories() // Set<PartPriceHistory>
                    .stream()
                    .filter(p -> p.getEndDate() == null || p.getEndDate().isAfter(LocalDate.now()))
                    .max((p1, p2) -> p1.getStartDate().compareTo(p2.getStartDate())) // lấy bản mới nhất
                    .map(PartPriceHistory::getPrice)
                    .orElse(0.0); // nếu ko có thì default 0

            SCExpense expense = SCExpense.builder()
                    .repairOrder(order)
                    .serviceCenter(order.getWarrantyClaim().getServiceCenter()) // set service center
                    .description("Chi phí part: " + d.getPart().getName())
                    .amount(price)
                    .status(SCExpense.ExpenseStatus.UNPAID) // set status mặc định
                    .paidDate(null) // chưa thanh toán
                    .build();
            scExpenseReposiotry.save(expense);
        }
    }

    public int handleCalculateResponseScore(long serviceCenterId) {
        List<RepairOrder> allOrders = repairOrderRepository.findAll();
        double avgResponseHours = allOrders.stream()
                .filter(o -> o.getWarrantyClaim() != null && o.getStartDate() != null)
                .mapToDouble(o -> Duration.between(o.getWarrantyClaim().getClaimDate(), o.getStartDate()).toHours())
                .average().orElse(0);

        return (int) Math.round(Math.max(0, 100 - avgResponseHours * 5)); // càng nhanh, điểm càng cao
    }

    public int handleCalculatePerformanceMetrics(long serviceCenterId) {
        List<RepairOrder> completedOrders = repairOrderRepository.findByServiceCenterIdAndStatus(serviceCenterId,
                RepairOrder.OrderStatus.COMPLETED);

        long onTimeCount = completedOrders.stream()
                .filter(o -> o.getStartDate() != null && o.getEndDate() != null)
                .filter(o -> Duration.between(o.getStartDate(), o.getEndDate()).toDays() <= 3) // thời hạn 3 ngày
                .count();

        return completedOrders.isEmpty() ? 0
                : (int) Math.round((onTimeCount * 100.0) / completedOrders.size());
    }

    public int handleCalculateOverdueRepairs(long serviceCenterId) {
        LocalDateTime now = LocalDateTime.now();

        long count = this.repairOrderRepository.findByServiceCenterId(serviceCenterId)
                .stream()
                .filter(o -> o.getStartDate() != null && o.getEstimated() > 0)
                .filter(o -> o.getEndDate() == null)
                .peek(order -> {
                    WarrantyClaim claim = order.getWarrantyClaim();

                    long daysSinceClaim = Duration.between(claim.getClaimDate(), now).toDays();

                    if (daysSinceClaim > 7) {
                        claim.setPriority(WarrantyClaim.ClaimPriority.HIGH);
                    } else if (order.getEstimated() <= 2) {
                        claim.setPriority(WarrantyClaim.ClaimPriority.NORMAL);
                    }

                    warrantyClaimRepository.save(claim); // lưu claim
                })
                // Lọc các order quá hạn (start + estimated < now)
                .filter(o -> o.getStartDate().plusDays(o.getEstimated()).isBefore(now))
                .count();

        return (int) count;
    }

    public int hanldeCalculateCompleteToday(long serviceCenterId) {
        LocalDate today = LocalDate.now();
        return repairOrderRepository.countByServiceCenterIdAndStatusAndEndDateBetween(
                serviceCenterId,
                RepairOrder.OrderStatus.COMPLETED,
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay());

    }

    public double handleCalculateAvgDays(long serviceCenterId) {
        List<RepairOrder> completedOrders = repairOrderRepository.findByServiceCenterIdAndStatus(
                serviceCenterId,
                RepairOrder.OrderStatus.COMPLETED);

        double avgDays = completedOrders.stream()
                .filter(o -> o.getStartDate() != null && o.getEndDate() != null)
                .mapToDouble(o -> Duration.between(o.getStartDate(), o.getEndDate()).toHours() / 24.0)
                .average()
                .orElse(0);

        return new BigDecimal(avgDays).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public int handleCalculateResolutionRate(long serviceCenterId) {
        long totalOrders = repairOrderRepository.countByServiceCenterId(serviceCenterId);
        long completedOrders = repairOrderRepository.countByServiceCenterIdAndStatus(serviceCenterId,
                RepairOrder.OrderStatus.COMPLETED);
        return totalOrders == 0 ? 0 : (int) Math.round((completedOrders * 100.0) / totalOrders);
    }

    public OrderSummaryResponse handleCalculateResolutionRateDifferent(long serviceCenterId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        LocalDate startOfPrevMonth = startOfMonth.minusMonths(1);
        LocalDate endOfPrevMonth = startOfMonth.minusDays(1);

        long totalCurrent = repairOrderRepository.countByServiceCenterIdAndStartDateBetween(
                serviceCenterId, startOfMonth.atStartOfDay(), endOfMonth.plusDays(1).atStartOfDay());
        long completedCurrent = repairOrderRepository.countByServiceCenterIdAndStatusAndStartDateBetween(
                serviceCenterId, RepairOrder.OrderStatus.COMPLETED,
                startOfMonth.atStartOfDay(), endOfMonth.plusDays(1).atStartOfDay());

        double currentRate = totalCurrent == 0 ? 0 : (completedCurrent * 100.0) / totalCurrent;

        long totalPrev = repairOrderRepository.countByServiceCenterIdAndStartDateBetween(
                serviceCenterId, startOfPrevMonth.atStartOfDay(), endOfPrevMonth.plusDays(1).atStartOfDay());
        long completedPrev = repairOrderRepository.countByServiceCenterIdAndStatusAndStartDateBetween(
                serviceCenterId, RepairOrder.OrderStatus.COMPLETED,
                startOfPrevMonth.atStartOfDay(), endOfPrevMonth.plusDays(1).atStartOfDay());

        double prevRate = totalPrev == 0 ? 0 : (completedPrev * 100.0) / totalPrev;

        double difference = (prevRate == 0) ? (currentRate > 0 ? 100 : 0)
                : ((currentRate - prevRate) / prevRate) * 100;

        return OrderSummaryResponse.builder()
                .averageResolution(new SummaryItemResponse((long) currentRate, "Average Resolution Time"))
                .diffirence(new SummaryItemResponse(
                        (long) Math.round(difference),
                        difference >= 0 ? "Increase" : "Decrease"))
                .build();
    }

    @Override
    public String sendRepairCompletedEmail(Long repairOrderId) {
        RepairOrder repairOrder = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new RuntimeException("Repair order not found"));

        if (repairOrder.getStatus() != RepairOrder.OrderStatus.COMPLETED) {
            throw new RuntimeException("Repair order is not completed yet");
        }
        // Join through relationships:
        WarrantyClaim claim = repairOrder.getWarrantyClaim();
        if (claim == null || claim.getVehicle() == null || claim.getVehicle().getCustomer() == null) {
            throw new RuntimeException("Customer information not found for this repair order");
        }

        Customer customer = claim.getVehicle().getCustomer();
        String customerName = customer.getName();
        String customerEmail = customer.getEmail();
        String vin = claim.getVehicle().getVin();

        // Create email content
        String subject = "Repair Completion Notification - OEM EV Warranty";
        String htmlContent = "<html><body>"
                + "<h3>Dear " + customerName + ",</h3>"
                + "<p>Your Vinfast" + claim.getVehicle().getModel().getName() + " (VIN: <b>" + vin
                + "</b>) has been successfully repaired.</p>"
                + "<p>Please visit our service center to pick up your vehicle.</p>"
                + "<p>If you have any questions, feel free to contact us.</p>"
                + "<p>Service center opens 7:00AM - 18:00PM from Monday to Tuesday</p>"
                + "<p>Thank you for trusting OEM EV Warranty service!</p>"
                + "<br><b>OEM EV Warranty Team</b>"
                + "</body></html>";

        // Build email request
        EmailDetailsRequest emailDetails = new EmailDetailsRequest();
        emailDetails.setRecipient(customerEmail);
        emailDetails.setSubject(subject);
        emailDetails.setMessageBody(htmlContent);

        // Send email
        emailService.sendHtmlMail(emailDetails);
        applicationEventPublisher.publishEvent(new EntityUpdatedEvent<>(this, repairOrder));
        return "Repair completion email sent to " + customerEmail;
    }
}