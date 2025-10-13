package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.CreateCampaignRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllCampaignResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ServiceCampaignDetailResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ServiceCampaignResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.CampaignVehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.CampaignVehicle.CampaignVehicleStatus;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ServiceCampaign;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Vehicle;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.CampaignVehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ServiceCampaignRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.VehicleRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.CampaignService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private final ServiceCampaignRepository serviceCampaignRepository;
    private final VehicleRepository vehicleRepository;
    private final CampaignVehicleRepository campaignVehicleRepository;

    @Override
    public ServiceCampaignResponse handleCreateCampaign(CreateCampaignRequest request) {

        if (serviceCampaignRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Campaign code already exists");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (request.getProduceDateFrom().isAfter(request.getProduceDateTo())) {
            throw new IllegalArgumentException("Produce date range is invalid");
        }

        ServiceCampaign campaign = ServiceCampaign.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .produceDateFrom(request.getProduceDateFrom())
                .produceDateTo(request.getProduceDateTo())
                .code(request.getCode())
                .build();

        serviceCampaignRepository.save(campaign);

        List<Vehicle> vehicles = vehicleRepository.findByProductionDateBetween(
        request.getProduceDateFrom(),
        request.getProduceDateTo());

        List<CampaignVehicle> campaignVehicles = vehicles.stream()
                .map(vehicle -> CampaignVehicle.builder()
                        .serviceCampaign(campaign)
                        .vehicle(vehicle)
                        .status(CampaignVehicleStatus.NOTIFIED)
                        .build())
                .toList();

        campaignVehicleRepository.saveAll(campaignVehicles);

        return ServiceCampaignResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .produceDateFrom(campaign.getProduceDateFrom())
                .produceDateTo(campaign.getProduceDateTo())
                .code(campaign.getCode())
                .totalVehicles(campaignVehicles.size())
                .build();
    }

    @Override
    public GetAllCampaignResponse handleGetAllCampaigns() {
        List<ServiceCampaignResponse> list = serviceCampaignRepository.findAll().stream()
                .map(c -> ServiceCampaignResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .description(c.getDescription())
                        .startDate(c.getStartDate())
                        .endDate(c.getEndDate())
                        .produceDateFrom(c.getProduceDateFrom())
                        .produceDateTo(c.getProduceDateTo())
                        .code(c.getCode())
                        .totalVehicles(c.getCampaignVehicles().size())
                        .build())
                .toList();

        return GetAllCampaignResponse.builder().campaigns(list).build();
    }

    @Override
    public ServiceCampaignDetailResponse handleGetCampaignById(Long id) {
        ServiceCampaign campaign = serviceCampaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));

        List<String> vins = campaign.getCampaignVehicles().stream()
                .map(cv -> cv.getVehicle().getVin())
                .toList();

        return ServiceCampaignDetailResponse.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .produceDateFrom(campaign.getProduceDateFrom())
                .produceDateTo(campaign.getProduceDateTo())
                .code(campaign.getCode())
                .vehicleVins(vins)
                .build();
    }

    @Override
    public void handleDeleteCampaign(Long id) {
        if (!serviceCampaignRepository.existsById(id)) {
            throw new IllegalArgumentException("Campaign not found");
        }
        serviceCampaignRepository.deleteById(id);
    }
}
