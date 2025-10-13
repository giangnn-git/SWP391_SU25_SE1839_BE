package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllVehicleResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetVehicleResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.VehicleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;

    @Override
    public void hanldeAddVehicle(Vehicle vehicle) {
        this.vehicleRepository.save(vehicle);
    }
    @Override
    public GetAllVehicleResponse getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        List<GetVehicleResponse> getVehicleResponses = vehicles.stream()
                .map(v -> GetVehicleResponse.builder()
                        .vin(v.getVin())
                        .modelName(v.getModel().getName())
                        .productYear(v.getProductYear()) 
                        .customerName(v.getCustomer() != null ? v.getCustomer().getName() : "N/A")
                        .build())
                .collect(Collectors.toList());

        return GetAllVehicleResponse.builder()
                .vehicles(getVehicleResponses)
                .build();
    }

}
