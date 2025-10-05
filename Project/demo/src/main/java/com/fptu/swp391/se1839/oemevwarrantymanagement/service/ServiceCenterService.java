package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCenter;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.User;

@Service
public interface ServiceCenterService {

    ServiceCenter handleAddCenter();

    ServiceCenter handleFindempolyee(User user);
}
