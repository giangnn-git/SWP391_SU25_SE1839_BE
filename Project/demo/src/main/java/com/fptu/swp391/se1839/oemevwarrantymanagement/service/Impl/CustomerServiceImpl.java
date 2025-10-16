package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CustomerRegisterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CustomerRegisterResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Customer;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.CustomerRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.CustomerService;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerServiceImpl implements CustomerService {

    final CustomerRepository customerRepository;
    final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public CustomerRegisterResponse registerCustomer(CustomerRegisterRequest req) {
        // Check VÍN
        var vehicle = vehicleRepository.findByVin(req.getVin())
                .orElseThrow(() -> new NoSuchElementException("Vehicle with VIN " + req.getVin() + " not found"));

        // Check vehicle đã có customer
        if (vehicle.getCustomer() != null) {
            throw new IllegalArgumentException("This vehicle already has a registered customer");
        }

        // Check email và phone
        customerRepository.findByPhoneNumber(req.getPhoneNumber())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Phone number already exists");
                });

        customerRepository.findByEmail(req.getEmail())
                .ifPresent(c -> {
                    throw new IllegalArgumentException("Email already exists");
                });

        // Lưu customer
        var customer = new Customer();
        customer.setName(req.getName());
        customer.setPhoneNumber(req.getPhoneNumber());
        customer.setEmail(req.getEmail());
        customer.setAddress(req.getAddress());

        var savedCustomer = customerRepository.save(customer);

        // Gắn customer vào vehicle
        vehicle.setCustomer(savedCustomer);
        vehicleRepository.save(vehicle);

        // Trả về CustomerResponse
        return new CustomerRegisterResponse(
                savedCustomer.getId(),
                savedCustomer.getName(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getEmail(),
                savedCustomer.getAddress());
    }
}