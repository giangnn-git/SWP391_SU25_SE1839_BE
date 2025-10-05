package com.fptu.swp391.se1839.oemevwarrantymanagement.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fptu.swp391.se1839.oemevwarrantymanagement.service.ClaimAttachmentService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import org.springframework.http.HttpHeaders;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class ClaimAttachmentController {

        private final ClaimAttachmentService claimAttachmentService;

        @GetMapping("/temp-file/{filename}")
        public ResponseEntity<Resource> getTempFile(@PathVariable String filename) throws IOException {
                Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "claim_uploads");
                Path filePath = tempDir.resolve(filename);

                if (!Files.exists(filePath)) {
                        return ResponseEntity.notFound().build();
                }

                Resource resource = new UrlResource(filePath.toUri());
                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, Files.probeContentType(filePath))
                                .body(resource);
        }

}
