package com.community.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // ================= OTP EMAIL =================
    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("🔑 Verify Your Email - Community Resource Sharing System");

            String content = "<html><body>" +
                             "<h2>👋 Welcome to Community Resource Sharing System!</h2>" +
                             "<p>Hi there,</p>" +
                             "<p>To complete your registration, please verify your email using the OTP below:</p>" +
                             "<h2 style='color:blue;'>" + otp + "</h2>" +
                             "<p>⚠️ This OTP is valid for 10 minutes only.</p>" +
                             "<p>With our platform, you can:</p>" +
                             "<ul>" +
                             "<li>📦 Share unused resources with the community.</li>" +
                             "<li>🤝 Borrow or rent items you need.</li>" +
                             "<li>🌱 Promote sustainability and reduce waste.</li>" +
                             "</ul>" +
                             "<p>We’re excited to have you onboard! 🚀</p>" +
                             "<p>Happy sharing! 💙</p>" +
                             "</body></html>";

            helper.setText(content, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    // ================= REGISTRATION CONFIRMATION =================
    @Async
    public void sendConfirmationEmail(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("🎉 Registration Successful - Community Resource Sharing System");

            String content = "<html><body>" +
                             "<h2>🎉 Welcome to Community Resource Sharing System!</h2>" +
                             "<p>Hi there! Your registration was successful.</p>" +
                             "<p>Here’s what you can do:</p>" +
                             "<ul>" +
                             "<li>📦 Share your unused resources with the community.</li>" +
                             "<li>🤝 Borrow or rent items you need at affordable rates.</li>" +
                             "<li>🌱 Promote sustainability and reduce waste.</li>" +
                             "</ul>" +
                             "<p>We’re thrilled to have you onboard! 🚀</p>" +
                             "<p>Enjoy sharing and connecting with your community! 💙</p>" +
                             "</body></html>";

            helper.setText(content, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }

    // ================= RETURN OTP TO OWNER =================
    @Async
    public void sendReturnOtpToOwner(
            String ownerEmail,
            String borrowerEmail,
            String resourceName,
            String otp) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(ownerEmail);
            helper.setSubject("🔁 Resource Return OTP - Community Resource Sharing System");

            String content = "<html><body>" +
                    "<h2>🔁 Resource Return Verification</h2>" +
                    "<p>Hello,</p>" +
                    "<p>The following user has initiated a resource return:</p>" +
                    "<p><b>User Email:</b> " + borrowerEmail + "</p>" +
                    "<p><b>Resource:</b> " + resourceName + "</p>" +
                    "<p>Please verify the return using the OTP below:</p>" +
                    "<h2 style='color:green;'>" + otp + "</h2>" +
                    "<p>⚠️ This OTP is valid for 10 minutes only.</p>" +
                    "<p>Thank you for using Community Resource Sharing System.</p>" +
                    "</body></html>";

            helper.setText(content, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send return OTP email to owner", e);
        }
    }

    // ================= RETURN CONFIRMATION TO BORROWER =================
    @Async
    public void sendReturnConfirmationToBorrower(String borrowerEmail, String resourceName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(borrowerEmail);
            helper.setSubject("✅ Resource Return Confirmed - Community Resource Sharing System");

            String content = "<html><body>" +
                    "<h2>✅ Resource Return Successful</h2>" +
                    "<p>Hello,</p>" +
                    "<p>Your return request for the following resource has been confirmed by the owner:</p>" +
                    "<p><b>Resource:</b> " + resourceName + "</p>" +
                    "<p>Thank you for using Community Resource Sharing System.</p>" +
                    "</body></html>";

            helper.setText(content, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send return confirmation email to borrower", e);
        }
    }

}
