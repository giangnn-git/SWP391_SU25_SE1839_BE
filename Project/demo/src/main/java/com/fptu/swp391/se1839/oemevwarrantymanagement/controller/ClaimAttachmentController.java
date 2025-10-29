package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.ApiResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.response.CustomerRegisterResponse;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.ClaimAttachmentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClaimAttachmentController {

        final ClaimAttachmentService claimAttachmentService;

        @Value("${attachment.base-path}")
        String attachmentBasePath;

        @PreAuthorize("hasAnyAuthority('ADMIN','EVM_STAFF')")
        @GetMapping("/temp-file/{filename}")
        public ResponseEntity<Resource> getTempFile(@PathVariable String filename) throws IOException {
                if (!filename.matches("^[a-zA-Z0-9._-]+$")) {
                        return ResponseEntity.badRequest().build();
                }

                Path filePath = Paths.get(attachmentBasePath).resolve(filename).normalize();
                if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                        return ResponseEntity.notFound().build();
                }

                Resource resource = new UrlResource(filePath.toUri());
                String contentType = Files.probeContentType(filePath);
                if (contentType == null)
                        contentType = "application/octet-stream";

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, contentType)
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                                .body(resource);
        }

        @DeleteMapping("claims/temp-file/{id}")
        public ResponseEntity<ApiResponse<String>> deleteTempFile(@PathVariable long attachmentId) throws IOException {
                String message = this.claimAttachmentService.handleDeleteTempFile(attachmentId);
                var result = ApiResponse.<String>builder()
                                .status(HttpStatus.CREATED.toString())
                                .message("Delete claim attachment successfully")
                                .data(message)
                                .build();
                return ResponseEntity.ok(result);
        }
}
