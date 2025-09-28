package vn.hoidanit.jobhunter.service;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.ServiceCenter;
import vn.hoidanit.jobhunter.domain.User;

@Service
public interface ServiceCenterService {

    ServiceCenter handleAddCenter();

    ServiceCenter handleFindempolyee(User user);
}