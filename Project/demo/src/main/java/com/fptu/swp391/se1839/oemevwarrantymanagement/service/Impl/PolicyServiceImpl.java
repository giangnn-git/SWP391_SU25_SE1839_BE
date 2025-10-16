package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreatePolicyRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UpdatePolicyRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreatePolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DeletePolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllPolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.UpdatePolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyPolicy;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartPolicyRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PolicyRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PolicyService;

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

                WarrantyPolicy policy = WarrantyPolicy.builder()
                                .name(request.getName())
                                .description(request.getDescription())
                                .durationPeriod(request.getDurationPeriod())
                                .mileageLimit(request.getMileageLimit())
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

        public UpdatePolicyResponse handleUpdatePolicy(Long policyId, UpdatePolicyRequest request) {
                WarrantyPolicy existingPolicy = policyRepository.findById(policyId)
                                .orElseThrow(() -> new NoSuchElementException(
                                                "Policy with ID " + policyId + " not found"));

                WarrantyPolicy otherPolicy = policyRepository.findByName(request.getName());
                if (otherPolicy != null && !otherPolicy.getId().equals(policyId)) {
                        throw new IllegalArgumentException("Warranty policy name already exists");
                }

                existingPolicy.setName(request.getName());
                existingPolicy.setDescription(request.getDescription());
                existingPolicy.setDurationPeriod(request.getDurationPeriod());
                existingPolicy.setMileageLimit(request.getMileageLimit());

                WarrantyPolicy updated = policyRepository.save(existingPolicy);
                log.info("Updated WarrantyPolicy with id: {}", updated.getId());

                return UpdatePolicyResponse.builder()
                                .policy(updated)
                                .build();
        }

        @Override
        public DeletePolicyResponse handleDeletePolicy(Long policyId) {
                WarrantyPolicy existingPolicy = policyRepository.findById(policyId)
                                .orElseThrow(() -> new NoSuchElementException(
                                                "Policy with ID " + policyId + " not found"));

                LocalDate today = LocalDate.now();

                // kiểm tra part policy còn hạn
                List<PartPolicy> unexpiredParts = partPolicyRepository.findUnexpiredPartPolicies(policyId, today);

                if (!unexpiredParts.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Cannot delete policy: there are still part policies that have not expired.");
                }

                policyRepository.delete(existingPolicy);
                log.info("Deleted WarrantyPolicy with id: {}", policyId);

                return DeletePolicyResponse.builder()
                                .success(true)
                                .message("Policy deleted successfully.")
                                .build();
        }

}