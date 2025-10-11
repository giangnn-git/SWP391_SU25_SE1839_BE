package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.PartListRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.GetAllPartResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartCategoryResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartListResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.PartResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.Part;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.PartRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.PartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PartServiceImpl implements PartService {
    private final PartRepository partRepository;

    public PartCategoryResponse handleListCategory() {
        boolean status = true;
        List<Part> partList = this.partRepository.findAll();
        Set<String> category = new HashSet<>();
        for (int i = 0; i < partList.size(); i++) {
            category.add(partList.get(i).getPartCategory());
        }
        if (category.size() == 0) {
            status = false;
        }
        return PartCategoryResponse.builder()
                .category(category)
                .status(true)
                .build();
    }

    public PartListResponse handlePartList(PartListRequest request) {
        List<Part> partList = this.partRepository.findByPartCategory(request.getName());
        Set<PartResponse> responseList = new HashSet<>();
        for (Part p : partList) {
            PartResponse pr = PartResponse.builder()
                    .id(p.getId())
                    .description(p.getDescription())
                    .name(p.getName())
                    .partCategory(p.getPartCategory())
                    .build();
            responseList.add(pr);
        }
        return PartListResponse.builder()
                .partList(responseList)
                .build();
    }

        public GetAllPartResponse handleGetPartList() {
            List<Part> partList = this.partRepository.findAll();
            List<PartResponse> responseList = new ArrayList<>();
            for (Part p : partList) {
                PartResponse pr = PartResponse.builder()
                        .id(p.getId())
                        .description(p.getDescription())
                        .name(p.getName())
                        .partCategory(p.getPartCategory())
                        .build();
                responseList.add(pr);
            }
            return GetAllPartResponse.builder()
                    .partList(responseList)
                    .build();
    }
}
