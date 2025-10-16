package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.DownloadImageRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.UploadImageRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.DownloadImageResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.ClaimAttachment;
import com.fptu.swp391.se1839.oemevwarrantymanagement.entity.WarrantyClaim;
import com.fptu.swp391.se1839.oemevwarrantymanagement.repository.ClaimAttachmentRepository;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.ClaimAttachmentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClaimAttachmentServiceImpl implements ClaimAttachmentService {

    final ClaimAttachmentRepository claimAttachmentRepository;

    public static byte[] compressImage(byte[] data) {
        Deflater deflater = new Deflater();
        deflater.setLevel(deflater.BEST_COMPRESSION);
        deflater.setInput(data);
        deflater.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4 * 1024];
        while (!deflater.finished()) {
            int size = deflater.deflate(tmp);
            outputStream.write(tmp, 0, size);
        }
        try {
            outputStream.close();
        } catch (Exception e) {
        }
        return outputStream.toByteArray();
    }

    public DownloadImageResponse downloadImage(DownloadImageRequest request) {
        Optional<ClaimAttachment> dbImageDate = this.claimAttachmentRepository.findByName(request.getNameFile());
        byte[] images = decompressImage(dbImageDate.get().getImageData());
        return DownloadImageResponse.builder()
                .file(images)
                .contentType(MediaType.IMAGE_PNG_VALUE)
                .build();
    }

    public static byte[] decompressImage(byte[] data) {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4 * 1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(tmp);
                outputStream.write(tmp, 0, count);
            }
        } catch (Exception e) {
        }
        return outputStream.toByteArray();
    }
}
