package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreatePartPolicyRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllPartPolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartPolicyCodeResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartPolicyDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartPolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartPolicyRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PolicyRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PartPolicyService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PartPolicyServiceImpl implements PartPolicyService {
        PartPolicyRepository partPolicyRepository;
        PartRepository partRepository;
        PolicyRepository policyRepository;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Override
        public GetAllPartPolicyResponse handleGetAllPartPolicies() {
                List<PartPolicy> partPolicies = partPolicyRepository.findAll();

                List<PartPolicyResponse> policyInfos = partPolicies.stream()
                                .map(p -> PartPolicyResponse.builder()
                                                .id(p.getId())
                                                .partName(p.getPart() != null ? p.getPart().getName() : null)
                                                .partCode(p.getPart() != null ? p.getPart().getCode() : null)
                                                .policyCode(p.getWarrantyPolicy() != null
                                                                ? p.getWarrantyPolicy().getCode()
                                                                : null)
                                                .startDate(p.getStartDate() != null ? p.getStartDate().format(formatter)
                                                                : null)
                                                .endDate(p.getEndDate() != null ? p.getEndDate().format(formatter)
                                                                : null)
                                                .build())
                                .collect(Collectors.toList());

                return GetAllPartPolicyResponse.builder()
                                .partPolicies(policyInfos)
                                .build();
        }

        @Override
        public PartPolicyDetailResponse handleGetPartPolicyById(Long partPolicyId) {
                PartPolicy partPolicy = partPolicyRepository.findById(partPolicyId)
                                .orElseThrow(() -> new IllegalArgumentException("Part Policy is not found"));

                Part part = partPolicy.getPart();
                WarrantyPolicy warranty = partPolicy.getWarrantyPolicy();

                return PartPolicyDetailResponse.builder()
                                .partName(part != null ? part.getName() : null)
                                .partCategory(part != null && part.getPartCategory() != null ? part.getPartCategory()
                                                : null)
                                .durationPeriod(warranty != null ? warranty.getDurationPeriod() : null)
                                .mileageLimit(warranty != null ? warranty.getMileageLimit() : null)
                                .description(warranty != null ? warranty.getDescription() : null)
                                .build();
        }

        @Override
        public PartPolicyResponse handleCreatePartPolicy(CreatePartPolicyRequest request) {
                try {
                        if (request.getPartCode() == null || request.getPolicyCode() == null)
                                throw new IllegalArgumentException("Part code and Policy code are required");
                        if (request.getStartDate() == null)
                                throw new IllegalArgumentException("Start date is required");

                        // ===== Lấy Part và Policy theo code =====
                        Part part = partRepository.findByCode(request.getPartCode())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Part not found with code: " + request.getPartCode()));

                        WarrantyPolicy policy = policyRepository.findByCode(request.getPolicyCode())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "WarrantyPolicy not found with code: "
                                                                        + request.getPolicyCode()));

                        LocalDate startDate = LocalDate.parse(request.getStartDate(), formatter);
                        LocalDate endDate = request.getEndDate() != null
                                        ? LocalDate.parse(request.getEndDate(), formatter)
                                        : null;

                        // ===== Validate logic =====
                        if (endDate != null && (endDate.isBefore(startDate) || endDate.isEqual(startDate))) {
                                throw new IllegalArgumentException("End date must be after start date");
                        }

                        // Cho phép ngày trong quá khứ nên không check < LocalDate.now()

                        boolean exists = partPolicyRepository.existsByPartIdAndWarrantyPolicyIdAndDateRangeOverlap(
                                        part.getId(), policy.getId(), startDate, endDate);
                        if (exists)
                                throw new IllegalArgumentException(
                                                "A policy for this part already exists during this period");

                        // ===== Nếu là promotion, kiểm tra và chia policy normal =====
                        if (policy.getType() == WarrantyPolicy.PolicyType.PROMOTION) {
                                List<PartPolicy> normalPolicies = partPolicyRepository
                                                .findOverlappingNormalPolicies(part.getId(), startDate, endDate);

                                for (PartPolicy normal : normalPolicies) {
                                        LocalDate nStart = normal.getStartDate();
                                        LocalDate nEnd = normal.getEndDate();

                                        if (nStart.isBefore(startDate) && (nEnd == null || nEnd.isAfter(endDate))) {
                                                // Chia làm 2
                                                normal.setEndDate(startDate.minusDays(1));
                                                partPolicyRepository.save(normal);

                                                PartPolicy secondHalf = PartPolicy.builder()
                                                                .part(part)
                                                                .warrantyPolicy(normal.getWarrantyPolicy())
                                                                .startDate(endDate.plusDays(1))
                                                                .endDate(nEnd)
                                                                .build();
                                                partPolicyRepository.save(secondHalf);
                                        }
                                }
                        }

                        // ===== Lưu chính sách mới =====
                        PartPolicy newPolicy = PartPolicy.builder()
                                        .part(part)
                                        .warrantyPolicy(policy)
                                        .startDate(startDate)
                                        .endDate(endDate)
                                        .build();

                        PartPolicy saved = partPolicyRepository.save(newPolicy);

                        return PartPolicyResponse.builder()
                                        .id(saved.getId())
                                        .partName(saved.getPart().getName())
                                        .partCode(saved.getPart().getCode())
                                        .policyCode(saved.getWarrantyPolicy().getCode())
                                        .startDate(formatter.format(saved.getStartDate()))
                                        .endDate(saved.getEndDate() != null ? formatter.format(saved.getEndDate())
                                                        : null)
                                        .build();

                } catch (DateTimeParseException e) {
                        throw new RuntimeException("Invalid date format, expected yyyy-MM-dd");
                }
        }

        @Override
        public PartPolicyCodeResponse handleGetPartPolicyCode() {
                List<Part> parts = partRepository.findAll();
                List<WarrantyPolicy> policies = policyRepository.findAll();

                List<String> partCodes = parts.stream()
                                .map(Part::getCode)
                                .collect(Collectors.toList());

                List<String> policyCodes = policies.stream()
                                .map(WarrantyPolicy::getCode)
                                .collect(Collectors.toList());

                return PartPolicyCodeResponse.builder()
                                .partCode(partCodes)
                                .policyCode(policyCodes)
                                .build();
        }
}
