package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ChangeStatusRepairDetailRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ChangeStatusRepairDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllRepairDetailResponse;

public interface RepairDetailService {
    GetAllRepairDetailResponse handleGetRepairDetail(long serviceCenterId, long repairOrderId);

    ChangeStatusRepairDetailResponse handleChangeStatus(
            ChangeStatusRepairDetailRequest request,
            Long repairDetailId);
}
