package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.DownloadImageRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UploadImageRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DownloadImageResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ClaimAttachment;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;

@Service
public interface ClaimAttachmentService {

    ClaimAttachment uploadImage(UploadImageRequest file, WarrantyClaim warrantyClaim) throws IOException;

    DownloadImageResponse downloadImage(DownloadImageRequest filename);

    byte[] compressImage(byte[] data);
}
