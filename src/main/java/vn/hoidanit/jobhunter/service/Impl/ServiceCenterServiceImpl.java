package vn.hoidanit.jobhunter.service.Impl;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.ServiceCenter;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.repository.ServiceCenterRepository;
import vn.hoidanit.jobhunter.service.ServiceCenterService;

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
