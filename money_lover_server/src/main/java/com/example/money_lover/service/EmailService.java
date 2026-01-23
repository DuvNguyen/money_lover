package com.example.money_lover.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine; // 1. Inject Thymeleaf Engine

    // Hàm gửi mail mới: Nhận vào tên template và biến dữ liệu
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // 2. Tạo Context để nạp dữ liệu vào template
            Context context = new Context();
            context.setVariables(variables);

            // 3. Render template thành chuỗi HTML
            String htmlBody = templateEngine.process(templateName, context);

            // 4. Gửi mail như cũ
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = bật chế độ HTML

            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Error sending email to {}: {}", to, e.getMessage());
        }
    }
}