package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CustomerRegisterRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CustomerRegisterResponse;

public interface CustomerService {
    CustomerRegisterResponse registerCustomer(CustomerRegisterRequest req);

    CustomerRegisterResponse handleFindCustomerByVin(String vin);

    CustomerRegisterResponse updateCustomer(Long id, CustomerRegisterRequest req);

}