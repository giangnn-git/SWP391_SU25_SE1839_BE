package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.domain.Vehicle;

@Service
public interface VehicleService {
    void hanldeAddVehicle(Vehicle vehicle);
}
