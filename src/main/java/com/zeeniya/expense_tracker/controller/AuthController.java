package com.zeeniya.expense_tracker.controller;

import com.zeeniya.expense_tracker.dto.AuthResponse;
import com.zeeniya.expense_tracker.dto.ForgotPasswordRequest;
import com.zeeniya.expense_tracker.dto.ResetPasswordRequest;
import com.zeeniya.expense_tracker.dto.UserLogInRequest;
import com.zeeniya.expense_tracker.dto.UserSignUpRequest;
import com.zeeniya.expense_tracker.service.PasswordResetService;
import com.zeeniya.expense_tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody UserSignUpRequest request){
        AuthResponse response = userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> logIn(@Valid @RequestBody UserLogInRequest request){
        AuthResponse response = userService.logIn(request);
        return ResponseEntity.ok(response);
    }

    // Same answer for known and unknown emails.
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request){
        passwordResetService.requestReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If an account exists for this email, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request){
        passwordResetService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }


}
