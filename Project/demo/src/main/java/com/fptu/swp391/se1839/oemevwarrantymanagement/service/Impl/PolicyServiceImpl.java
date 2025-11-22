package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreatePolicyRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UpdatePolicyRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreatePolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllPolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.UpdatePolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartPolicyRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PolicyRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PolicyService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PolicyServiceImpl implements PolicyService {
        final PolicyRepository policyRepository;
        final PartPolicyRepository partPolicyRepository;

        @Override
        public GetAllPolicyResponse handleGetAllPolicy() {
                List<WarrantyPolicy> policies = policyRepository.findAll();

                List<PolicyResponse> policiesResponse = policies.stream()
                                .map(policy -> PolicyResponse.builder()
                                                .id(policy.getId())
                                                .name(policy.getName())
                                                .description(policy.getDescription())
                                                .durationPeriod(policy.getDurationPeriod())
                                                .mileageLimit(policy.getMileageLimit())
                                                .code(policy.getCode())
                                                .policyType(PolicyResponse.PolicyType.valueOf(policy.getType().name()))
                                                .status(PolicyResponse.Status.valueOf(policy.getStatus().name()))
                                                .build())
                                .toList();

                return GetAllPolicyResponse.builder()
                                .policyList(policiesResponse)
                                .build();
        }

        @Override
        public CreatePolicyResponse handleCreatePolicy(CreatePolicyRequest request) {
                if (policyRepository.existsByName(request.getName())) {
                        return CreatePolicyResponse.builder()
                                        .success(false)
                                        .message("Warranty policy name already exists")
                                        .policy(null)
                                        .build();
                }
                if (policyRepository.existsByCode(request.getCode())) {
                        return CreatePolicyResponse.builder()
                                        .success(false)
                                        .message("Warranty policy code already exists")
                                        .policy(null)
                                        .build();
                }
                if (request.getDurationPeriod() <= 0) {
                        return CreatePolicyResponse.builder()
                                        .success(false)
                                        .message("Duration period must be greater than 0")
                                        .policy(null)
                                        .build();
                }
                if (request.getMileageLimit() <= 0) {
                        return CreatePolicyResponse.builder()
                                        .success(false)
                                        .message("Mileage limit must be greater than 0")
                                        .policy(null)
                                        .build();
                }
                if (request.getDescription() == null || request.getDescription().isEmpty()) {
                        return CreatePolicyResponse.builder()
                                        .success(false)
                                        .message("Description cannot be empty")
                                        .policy(null)
                                        .build();
                }
                if (request.getDescription().length() > 255) {
                        return CreatePolicyResponse.builder()
                                        .success(false)
                                        .message("Description cannot exceed 255 characters")
                                        .policy(null)
                                        .build();
                }
                WarrantyPolicy policy = WarrantyPolicy.builder()
                                .code(request.getCode())
                                .name(request.getName())
                                .description(request.getDescription())
                                .durationPeriod(request.getDurationPeriod())
                                .mileageLimit(request.getMileageLimit())
                                .type(request.getType() == null ? WarrantyPolicy.PolicyType.NORMAL
                                                : WarrantyPolicy.PolicyType.valueOf(request.getType().name()))
                                .status(WarrantyPolicy.Status.ACTIVE)
                                .build();

                WarrantyPolicy saved = policyRepository.save(policy);
                log.info("Created new WarrantyPolicy with id: {}", saved.getId());

                return CreatePolicyResponse.builder()
                                .success(true)
                                .message("Warranty policy created successfully")
                                .policy(saved)
                                .build();
        }

        @Override
        public PolicyResponse handleGetPolicyById(Long policyId) {
                WarrantyPolicy policy = policyRepository.findById(policyId)
                                .orElseThrow(() -> new NoSuchElementException("Policy is not existed"));
                return PolicyResponse.builder()
                                .id(policy.getId())
                                .name(policy.getName())
                                .description(policy.getDescription())
                                .durationPeriod(policy.getDurationPeriod())
                                .mileageLimit(policy.getMileageLimit())
                                .build();
        }

        @Override
        @Transactional
        public UpdatePolicyResponse handleUpdatePolicy(Long policyId, UpdatePolicyRequest request) {

                WarrantyPolicy existing = policyRepository.findById(policyId)
                                .orElseThrow(() -> new NoSuchElementException(
                                                "Policy with ID " + policyId + " not found"));

                boolean isAssigned = partPolicyRepository.existsByWarrantyPolicyId(policyId);

                if (request.getPolicyType() != null &&
                                !request.getPolicyType().name().equals(existing.getType().name())) {
                        throw new IllegalArgumentException(
                                        "Policy type cannot be modified once created");
                }

                if (request.getDescription() == null || request.getDescription().isEmpty()) {
                        throw new IllegalArgumentException("Description cannot be empty");
                }
                if (request.getDescription().length() > 255) {
                        throw new IllegalArgumentException("Description cannot exceed 255 characters");
                }

                if (isAssigned) {
                        throw new IllegalArgumentException(
                                        "This policy has already been assigned.");
                } else {
                        WarrantyPolicy policyWithSameName = policyRepository.findByName(request.getName());
                        if (policyWithSameName != null && !policyWithSameName.getId().equals(policyId)) {
                                throw new IllegalArgumentException("Warranty policy name already exists");
                        }
                        existing.setName(request.getName());
                        existing.setDescription(request.getDescription());
                        if (request.getDurationPeriod() != null) {
                                if (request.getDurationPeriod() <= 0) {
                                        throw new IllegalArgumentException("Duration period must be greater than 0");
                                }
                                existing.setDurationPeriod(request.getDurationPeriod());
                        }

                        if (request.getMileageLimit() != null) {
                                if (request.getMileageLimit() <= 0) {
                                        throw new IllegalArgumentException("Mileage limit must be greater than 0");
                                }
                                existing.setMileageLimit(request.getMileageLimit());
                        }
                }

                WarrantyPolicy updated = policyRepository.save(existing);

                return UpdatePolicyResponse.builder()
                                .policy(updated)
                                .build();
        }
}