package com.example.money_lover.service;

import com.example.money_lover.exception.AppException;
import com.example.money_lover.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            // 1. Tạo MimeMessage (Hỗ trợ HTML, đính kèm file...)
            MimeMessage message = javaMailSender.createMimeMessage();
            
            // 2. Dùng Helper để set thông tin dễ hơn
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            // Tham số thứ 2 là 'true' -> Bật chế độ HTML
            helper.setText(htmlBody, true); 

            // 3. Gửi
            javaMailSender.send(message);
            log.info("Email sent successfully to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Error sending email: {}", e.getMessage());
            // Tùy bạn: có thể throw lỗi ra để Controller biết
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}