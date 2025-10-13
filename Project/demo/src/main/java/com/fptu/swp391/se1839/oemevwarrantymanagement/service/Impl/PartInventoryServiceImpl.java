package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartInventoryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.PartInventory;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartInventoryRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PartInventoryService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PartInventoryServiceImpl implements PartInventoryService {

    private final PartInventoryRepository partInventoryRepository;

    @Override
    public List<PartInventoryResponse> getAllPartInventories() {
        return partInventoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PartInventoryResponse> getPartInventoriesByServiceCenter(Long serviceCenterId) {
        return partInventoryRepository.findByServiceCenter_Id(serviceCenterId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PartInventoryResponse mapToResponse(PartInventory pi) {
        return PartInventoryResponse.builder()
                .id(pi.getId())
                .partId(pi.getPart().getId())
                .partName(pi.getPart().getName())
                .partCategory(pi.getPart().getPartCategory())
                .serviceCenterId(pi.getServiceCenter().getId())
                .serviceCenterName(pi.getServiceCenter().getName())
                .serviceCenterAddress(pi.getServiceCenter().getAddress())
                .quantity(pi.getQuantity())
                .unit(pi.getPart().getUnit().toString())
                .build();
    }
}
