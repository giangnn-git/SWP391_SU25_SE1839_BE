package vn.hoidanit.jobhunter.service.Impl;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Vehicle;
import vn.hoidanit.jobhunter.repository.VehicleRepository;
import vn.hoidanit.jobhunter.service.VehicleService;

@Service
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void hanldeAddVehicle(Vehicle vehicle) {
        this.vehicleRepository.save(vehicle);
    }

}
