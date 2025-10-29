package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDateTime;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fptu.swp391.se1839.oemevwarrantymanagement.annotation.Activity;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChooseTechnicalRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ActivityLog;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairOrder;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ActivityLogRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.ActivityLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Aspect
@Service
@Transactional
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(activity)")
    public Object around(ProceedingJoinPoint joinPoint, Activity activity) throws Throwable {
        Object result = null;
        Exception error = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            error = e;
            throw e;
        } finally {
            try {
                handleActivity(joinPoint, activity, result, error);
            } catch (Exception e) {
                System.err.println("[ActivityLogAspect] Failed to log: " + e.getMessage());
            }
        }
    }

    private void handleActivity(ProceedingJoinPoint joinPoint, Activity activity, Object result, Exception error)
            throws Exception {

        for (Object arg : joinPoint.getArgs()) {

            // Log cho RepairOrder
            if (arg instanceof RepairOrder ro) {
                // Cách 2: log nếu status trùng với annotation
                if (ro.getStatus().toString().equalsIgnoreCase(activity.status())) {
                    saveActivityLog(joinPoint, activity, ro, error);
                }
                return;
            }

            // Log cho List<RepairOrder>
            if (arg instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof RepairOrder roItem) {
                        if (roItem.getStatus().toString().equalsIgnoreCase(activity.status())) {
                            saveActivityLog(joinPoint, activity, roItem, error);
                        }
                    }
                }
                return;
            }
        }
    }

    private void saveActivityLog(ProceedingJoinPoint joinPoint, Activity activity, RepairOrder order, Exception error)
            throws Exception {

        WarrantyClaim claim = order.getWarrantyClaim();
        Vehicle vehicle = claim != null ? claim.getVehicle() : null;

        ActivityLog log = new ActivityLog();
        log.setTitle(activity.title());
        log.setStatus(error == null ? activity.status() : "FAILED");
        log.setCreatedAt(LocalDateTime.now());
        log.setClaim(claim);
        log.setVehicle(vehicle);

        if (vehicle != null) {
            log.setDescription(vehicle.getModel().getName() +
                    " - VIN: " + vehicle.getVin() +
                    " - License Plate: " + vehicle.getLicensePlate());
        } else {
            log.setDescription("No vehicle information available");
        }

        // Xử lý detail với placeholder
        String detail = activity.detail();
        detail = detail.replace("{orderId}", String.valueOf(order.getId()));
        if (claim != null)
            detail = detail.replace("{claimId}", String.valueOf(claim.getId()));

        // Nếu có technicalName trong request
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ChooseTechnicalRequest req) {
                detail = detail.replace("{technicalName}", req.getTechnicalName());
            }
        }

        log.setDetail(detail);

        // Meta info
        var meta = objectMapper.createObjectNode();
        meta.put("ip", request.getRemoteAddr());
        meta.put("method", joinPoint.getSignature().toShortString());
        meta.put("timestamp", LocalDateTime.now().toString());
        if (error != null)
            meta.put("error", error.getMessage());

        log.setMeta(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(meta));

        activityLogRepository.save(log);
    }

    @Override
    public List<ActivityLog> findRecentActivities() {
        return activityLogRepository.findTop5ByOrderByCreatedAtDesc();
    }
}
