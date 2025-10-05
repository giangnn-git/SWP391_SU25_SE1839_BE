package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CustomerRegisterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Customer;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.CustomerRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.CustomerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public Customer registerCustomer(CustomerRegisterRequest req) {
        // check vin
        Vehicle vehicle = vehicleRepository.findByVin(req.getVin());
        if (vehicle == null) {
            throw new NoSuchElementException("Vehicle with VIN " + req.getVin() + " not found");
        }

        // check customer
        if (vehicle.getCustomer() != null) {
            throw new IllegalArgumentException("This vehicle already has a registered customer");
        }

        // check email and phone
        customerRepository.findByPhoneNumber(req.getPhoneNumber())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Phone number already exists");
                });

        customerRepository.findByEmail(req.getEmail())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        // save
        Customer customer = new Customer();
        customer.setName(req.getName());
        customer.setPhoneNumber(req.getPhoneNumber());
        customer.setEmail(req.getEmail());
        customer.setAddress(req.getAddress());

        Customer savedCustomer = customerRepository.save(customer);

        vehicle.setCustomer(savedCustomer);
        vehicleRepository.save(vehicle);

        return savedCustomer;
    }
}