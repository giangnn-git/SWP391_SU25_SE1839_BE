package com.fptu.swp391.se1839.oemevwarrantymanagement.service;

import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.EmailDetailsRequest;

@Service
public interface EmailService {
    String sendHtmlMail(EmailDetailsRequest details);
}
