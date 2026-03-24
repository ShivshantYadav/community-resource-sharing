package com.community.controller;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.community.entity.User;
import com.community.entity.Role;
import com.community.repository.UserRepository;
import com.community.security.JwtUtil;
import com.community.service.AuthService;
import com.community.service.UserService;
import com.community.service.EmailService;
import com.community.dto.RegisterRequest;
import com.community.dto.ResetPasswordRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService; // ✅ Added

    // ================= REGISTER =================
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request,
            BindingResult bindingResult) {

        // Validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));
            return ResponseEntity.badRequest().body(errors);
        }

        // Email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("email", "Email already registered"));
        }

        // Password not null
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("password", "Password cannot be null or empty"));
        }

        // Create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);
        user.setPassword(request.getPassword()); // hashing handled in service

        userService.register(user); // Sends OTP

        return ResponseEntity.ok(
                Map.of("message", "Registration successful. OTP sent to email. Please verify your account")
        );
    }

    // ================= VERIFY EMAIL =================
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp = body.get("otp");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(java.time.LocalDateTime.now())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "OTP expired"));
        }

        if (!otp.equals(user.getOtp())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid OTP"));
        }

        // ✅ Mark email verified
        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        // ✅ Send registration success email
        emailService.sendConfirmationEmail(user.getEmail());

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Email verified successfully. Confirmation email sent.",
                        "token", token
                )
        );
    }

    // ================= LOGIN =================
 // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {

        String email = loginData.get("email");
        String password = loginData.get("password");

        User user;
        try {
            user = userService.authenticate(email, password);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid email or password"));
        }

        if (!user.isEmailVerified()) {
            return ResponseEntity.status(400)
                    .body(Map.of("message", "Please verify your email first"));
        }

        // ✅ Check if user is blocked
        if (!user.isEnabled()) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Your account has been blocked by admin"));
        }


        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "userId", user.getUserId()
                )
        );
    }



    // ================= FORGOT PASSWORD =================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        authService.sendOtp(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "OTP sent to email"));
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}
