package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Vehicle;

@Service
public interface VehicleService {
    void hanldeAddVehicle(Vehicle vehicle);
}
