package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreatePartPolicyRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllPartPolicyResponse;
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
                                                .policyId(p.getWarrantyPolicy() != null ? p.getWarrantyPolicy().getId()
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
                        if (request.getPartId() == null || request.getWarrantyPolicyId() == null)
                                throw new IllegalArgumentException("Part ID and Warranty Policy ID are required");
                        if (request.getStartDate() == null)
                                throw new IllegalArgumentException("Start date is required");

                        Part part = partRepository.findById(request.getPartId())
                                        .orElseThrow(() -> new IllegalArgumentException("Part not found"));
                        WarrantyPolicy policy = policyRepository.findById(request.getWarrantyPolicyId())
                                        .orElseThrow(() -> new IllegalArgumentException("WarrantyPolicy not found"));

                        LocalDate startDate = LocalDate.parse(request.getStartDate(), formatter);
                        LocalDate endDate = request.getEndDate() != null
                                        ? LocalDate.parse(request.getEndDate(), formatter)
                                        : null;

                        if (endDate != null && endDate.isBefore(startDate))
                                throw new IllegalArgumentException("End date cannot be before start date");

                        if (startDate.isBefore(LocalDate.now()))
                                throw new IllegalArgumentException("Start date cannot be in the past");

                        if (request.getEndDate() != null) {
                                LocalDate requestEndDate = LocalDate.parse(request.getEndDate(), formatter);
                                if (requestEndDate.isBefore(LocalDate.now())) {
                                        throw new IllegalArgumentException(
                                                        "Warranty Policy end date cannot be in the past");
                                }
                        }

                        boolean exists = partPolicyRepository.existsByPartIdAndWarrantyPolicyIdAndDateRangeOverlap(
                                        request.getPartId(), request.getWarrantyPolicyId(), startDate, endDate);
                        if (exists)
                                throw new IllegalArgumentException(
                                                "A policy for this part already exists during this period");

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
                                        .policyId(saved.getWarrantyPolicy().getId())
                                        .startDate(formatter.format(saved.getStartDate()))
                                        .endDate(saved.getEndDate() != null ? formatter.format(saved.getEndDate())
                                                        : null)
                                        .build();

                } catch (DateTimeParseException e) {
                        throw new RuntimeException("Invalid date format, expected yyyy-MM-dd");
                }
        }
}
