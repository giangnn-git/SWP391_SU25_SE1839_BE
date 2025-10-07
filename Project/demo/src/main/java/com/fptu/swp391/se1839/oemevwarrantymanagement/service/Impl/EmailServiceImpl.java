package com.fptu.swp391.se1839.oemevwarrantymanagement.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.fptu.swp391.se1839.oemevwarrantymanagement.dto.request.EmailDetailsRequest;
import com.fptu.swp391.se1839.oemevwarrantymanagement.service.EmailService;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    // Lấy email người gửi từ application.properties
    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public String sendHtmlMail(EmailDetailsRequest details) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Cài đặt thông tin người gửi
            mimeMessageHelper.setFrom(sender);

            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setSubject(details.getSubject());

            // Tham số 'true' chỉ định nội dung là HTML
            mimeMessageHelper.setText(details.getMessageBody(), true);

            javaMailSender.send(mimeMessage);
            return "HTML Email sent successfully to " + details.getRecipient();
        } catch (Exception e) {
            System.err.println("Error while sending HTML email: " + e.getMessage());
            return "Error while sending HTML email: " + e.getMessage();
        }
    }
}