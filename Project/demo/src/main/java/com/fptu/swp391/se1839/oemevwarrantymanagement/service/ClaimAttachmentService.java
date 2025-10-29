package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import java.io.IOException;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.DownloadImageRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DownloadImageResponse;

public interface ClaimAttachmentService {

    DownloadImageResponse downloadImage(DownloadImageRequest filename);

    String handleDeleteTempFile(long attachmentId) throws IOException;
}
