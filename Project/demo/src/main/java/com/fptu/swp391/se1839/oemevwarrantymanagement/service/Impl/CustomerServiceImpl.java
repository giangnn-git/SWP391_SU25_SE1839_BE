package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.AddVehicleRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CustomerRegisterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CustomerRegisterResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CustomerSummaryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.RegisteredVehicleResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.VehicleInfoResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.CampaignVehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Customer;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.CustomerRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.UserRepository;
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
    final UserRepository userRepository;
    final ServiceCenterRepository scRepository;

    @Override
    @Transactional
    public CustomerRegisterResponse registerCustomer(CustomerRegisterRequest req, Long userId, Long scId) {
        // Check VÍN
        var vehicle = vehicleRepository.findByVin(req.getVin())
                .orElseThrow(() -> new NoSuchElementException(
                "Vehicle with VIN " + req.getVin() + " not found"));

        // Check vehicle đã có customer
        if (vehicle.getCustomer() != null) {
            throw new IllegalArgumentException("This vehicle already has a registered customer");
        }

        if (vehicleRepository.existsByLicensePlate(req.getLicensePlate())) {
            throw new IllegalArgumentException("License plate already exists");
        }

        if (userId == null || userRepository.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("User ID is invalid");
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
        customer.setCreatedBy(userRepository.findById(userId).get());
        customer.setServiceCenter(scRepository.findById(scId).get());

        var savedCustomer = customerRepository.save(customer);

        // Gắn customer vào vehicle
        vehicle.setCustomer(savedCustomer);
        vehicle.setLicensePlate(req.getLicensePlate());
        vehicleRepository.save(vehicle);

        // Trả về CustomerResponse
        return new CustomerRegisterResponse(
                savedCustomer.getId(),
                savedCustomer.getName(),
                savedCustomer.getPhoneNumber(),
                savedCustomer.getEmail(),
                savedCustomer.getAddress());
    }

    @Override
    public CustomerRegisterResponse handleFindCustomerByVin(String vin) {
        var vehicle = vehicleRepository.findByVin(vin)
                .orElseThrow(() -> new NoSuchElementException(
                "Vehicle with VIN: " + vin + " not found"));
        Customer customer = customerRepository.findById(vehicle.getCustomer().getId())
                .orElseThrow(() -> new NoSuchElementException(
                "Customer with VIN " + vin + " not found"));
        return new CustomerRegisterResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                customer.getAddress());
    }

    @Override
    public CustomerRegisterResponse updateCustomer(Long id, CustomerRegisterRequest req) {
        // 1️⃣ Tìm customer
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));

        customerRepository.findByPhoneNumber(req.getPhoneNumber())
                .ifPresent(existing -> {
                    if (!Objects.equals(existing.getId(), id)) {
                        throw new IllegalArgumentException("Phone number already exists");
                    }
                });

        customerRepository.findByEmail(req.getEmail())
                .ifPresent(existing -> {
                    if (!Objects.equals(existing.getId(), id)) {
                        throw new IllegalArgumentException("Email already exists");
                    }
                });

        // 4️⃣ Nếu request có biển số → kiểm tra & cập nhật
        if (req.getLicensePlate() != null && !req.getLicensePlate().isBlank()) {
            vehicleRepository.findByLicensePlate(req.getLicensePlate())
                    .ifPresent(existingVehicle -> {
                        if (existingVehicle.getCustomer() != null
                                && !Objects.equals(
                                        existingVehicle.getCustomer().getId(),
                                        id)) {
                            throw new IllegalArgumentException(
                                    "License plate already belongs to another customer");
                        }
                    });

            // Lấy xe hiện tại của customer để cập nhật
            var vehicle = vehicleRepository.findByVin(req.getVin()).orElse(null);
            if (vehicle != null) {
                vehicle.setLicensePlate(req.getLicensePlate());
                vehicleRepository.save(vehicle);
            }
        }

        // 5️⃣ Cập nhật thông tin customer
        customer.setName(req.getName());
        customer.setPhoneNumber(req.getPhoneNumber());
        customer.setEmail(req.getEmail());
        customer.setAddress(req.getAddress());

        var saved = customerRepository.save(customer);

        // 6️⃣ Trả về response
        return new CustomerRegisterResponse(
                saved.getId(),
                saved.getName(),
                saved.getPhoneNumber(),
                saved.getEmail(),
                saved.getAddress());
    }

    @Override
    @Transactional
    public CustomerRegisterResponse addVehicleForExistingCustomer(Long customerId, AddVehicleRequest req) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("Customer not found"));

        var vehicle = vehicleRepository.findByVin(req.getVin())
                .orElseThrow(() -> new NoSuchElementException(
                "Vehicle with VIN " + req.getVin() + " not found"));

        if (vehicle.getCustomer() != null) {
            throw new IllegalArgumentException("This vehicle already has an owner");
        }

        if (vehicleRepository.existsByLicensePlate(req.getLicensePlate())) {
            throw new IllegalArgumentException("License plate already exists");
        }

        vehicle.setCustomer(customer);
        vehicle.setLicensePlate(req.getLicensePlate());
        vehicleRepository.save(vehicle);

        return new CustomerRegisterResponse(
                customer.getId(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getEmail(),
                customer.getAddress());
    }

    @Override
    public List<RegisteredVehicleResponse> getAllRegisteredVehicles() {
        return vehicleRepository.findAllRegisteredVehicles()
                .stream()
                .map(v -> RegisteredVehicleResponse.builder()
                .vin(v.getVin())
                .licensePlate(v.getLicensePlate())
                .purchaseDate(v.getPurchaseDate())
                .modelName(v.getModel().getName())
                .customerName(v.getCustomer().getName())
                .customerPhone(v.getCustomer().getPhoneNumber())
                .scID(v.getCustomer() == null ? null : v.getCustomer().getCreatedBy().getId())
                .build())
                .toList();
    }

    @Override
    public List<CustomerSummaryResponse> getAllCustomerSummaries() {
        List<Customer> customers = customerRepository.findAll();

        return customers.stream()
                .map(customer -> CustomerSummaryResponse.builder()
                .id(customer.getId())
                .name(customer.getName())
                .phoneNumber(customer.getPhoneNumber())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .vehicleCount(customer.getVehicles() != null
                        ? customer.getVehicles().size()
                        : 0)
                .scId(customer.getServiceCenter().getId())
                .build())
                .toList();
    }

    @Override
    public List<VehicleInfoResponse> getVehiclesByCustomerId(Long customerId) {
        List<Vehicle> vehicles = vehicleRepository.findAllByCustomerIdWithCampaigns(customerId);

        return vehicles.stream()
                .map(v -> VehicleInfoResponse.builder().vin(v.getVin())
                .modelName(v.getModel().getName())
                .licensePlate(v.getLicensePlate())
                .purchaseDate(v.getPurchaseDate())
                .campaignNames(v.getCampaignVehicles() != null
                        ? v.getCampaignVehicles().stream().filter(cv -> cv
                .getStatus() != CampaignVehicle.CampaignVehicleStatus.COMPLETED)
                                .map(cv -> cv.getServiceCampaign()
                                .getName())
                                .distinct()
                                .toList()
                        : List.<String>of())
                .build())
                .toList();
    }

}
