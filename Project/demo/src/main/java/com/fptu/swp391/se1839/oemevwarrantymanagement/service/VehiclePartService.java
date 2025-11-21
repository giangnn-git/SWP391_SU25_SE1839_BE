package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ScanSerialNumberRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ScanSerialNumberResponse;

public interface VehiclePartService {
    ScanSerialNumberResponse handleCreateNewSerialNumber(ScanSerialNumberRequest request, Long repairDetailId);

}
