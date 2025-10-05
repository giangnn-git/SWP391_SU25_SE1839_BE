package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCenterRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.ServiceCenterService;

@Service
public class ServiceCenterServiceImpl implements ServiceCenterService {

    private final ServiceCenterRepository serviceCenterRepository;

    public ServiceCenterServiceImpl(ServiceCenterRepository serviceCenterRepository) {
        this.serviceCenterRepository = serviceCenterRepository;
    }

    @Override
    public ServiceCenter handleAddCenter() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ServiceCenter handleFindempolyee(User user) {
        return this.serviceCenterRepository.findById(user.getServiceCenter().getId())
                .orElseThrow(() -> new NoSuchElementException("No find Service Center"));
    }
}
