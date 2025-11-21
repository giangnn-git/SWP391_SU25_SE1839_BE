package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.ScanSerialNumberRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ScanSerialNumberResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartInventory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.RepairDetail;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.VehiclePart;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartInventoryRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.RepairDetailRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehiclePartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.VehiclePartService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehiclePartServiceImpl implements VehiclePartService {

    final VehiclePartRepository vehiclePartRepository;
    final PartInventoryRepository partInventoryRepository;
    final RepairDetailRepository repairDetailRepository;

    @Override
    @Transactional
    public ScanSerialNumberResponse handleCreateNewSerialNumber(ScanSerialNumberRequest request, Long repairDetailId) {
        // Tìm VehiclePart dựa vào oldSerialNumber
        VehiclePart oldVP = vehiclePartRepository.findByOldSerialNumber(request.getOldSerialNumber());
        RepairDetail rd = repairDetailRepository.findById(repairDetailId)
                .orElseThrow(() -> new NoSuchElementException("RepairDetail not found with id: " + repairDetailId));
        if (oldVP == null) {
            throw new NoSuchElementException("VehiclePart with old serial number not found: "
                    + request.getOldSerialNumber());
        }

        // Tạo serial number mới cho oldVP
        oldVP.setNewSerialNumber(request.getNewSerialNumber());
        vehiclePartRepository.save(oldVP);

        // Tạo một VehiclePart mới dựa trên oldVP (nếu cần giống logic Assembly)
        VehiclePart newVP = VehiclePart.builder()
                .vehicle(oldVP.getVehicle())
                .part(oldVP.getPart())
                .oldSerialNumber(request.getNewSerialNumber())
                .installationDate(LocalDate.now())
                .build();
        vehiclePartRepository.save(newVP);

        Long partId = oldVP.getPart().getId();
        Long scId = rd.getRepairOrder().getWarrantyClaim().getServiceCenter().getId();

        PartInventory inventory = partInventoryRepository
                .findByPartIdAndServiceCenterId(partId, scId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for this part at this SC"));

        long newQty = inventory.getQuantity() - 1;

        if (newQty < 0) {
                throw new RuntimeException("Inventory not enough to assign this serial");
        }

        inventory.setQuantity(newQty);
        partInventoryRepository.save(inventory);

        return ScanSerialNumberResponse.builder()
                .now(LocalDate.now())
                .build();
    }

}
