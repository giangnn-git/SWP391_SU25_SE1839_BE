package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateRepairStepRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChangeStatusRepairStepResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CreateRepairStepResponse;

public interface RepairStepService {
    CreateRepairStepResponse handleCreateRepairStep(CreateRepairStepRequest request, long repairOrderId);

    ChangeStatusRepairStepResponse changeStepStatus(long repairStepId, String newStatusStr);
}
