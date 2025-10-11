package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreatePolicyRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UpdatePolicyRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreatePolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DeletePolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllPolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PolicyResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.UpdatePolicyResponse;

@Service
public interface PolicyService {
    GetAllPolicyResponse handleGetAllPolicy();
    CreatePolicyResponse handleCreatePolicy(CreatePolicyRequest request);
    PolicyResponse handleGetPolicyById(Long policyId);
    UpdatePolicyResponse handleUpdatePolicy(Long policyId, UpdatePolicyRequest request);
    DeletePolicyResponse handleDeletePolicy(Long policyId);
    
}
