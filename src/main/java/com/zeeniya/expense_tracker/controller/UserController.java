package com.zeeniya.expense_tracker.controller;

import com.zeeniya.expense_tracker.dto.UpdateProfileRequest;
import com.zeeniya.expense_tracker.dto.UserResponse;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal User currentUser) {
        return userService.getProfile(currentUser);
    }

    @PutMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateProfileRequest request, @AuthenticationPrincipal User currentUser) {
        return userService.updateProfile(currentUser, request);
    }
}
