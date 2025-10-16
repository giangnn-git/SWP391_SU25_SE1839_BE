package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.VehicleService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleServiceImpl implements VehicleService {
    private VehicleRepository vehicleRepository;

    @Override
    public void hanldeAddVehicle(Vehicle vehicle) {
        this.vehicleRepository.save(vehicle);
    }

}
