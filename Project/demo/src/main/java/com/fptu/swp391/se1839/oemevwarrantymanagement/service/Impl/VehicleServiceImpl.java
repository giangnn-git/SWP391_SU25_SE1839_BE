package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import org.springframework.stereotype.Service;

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

}
