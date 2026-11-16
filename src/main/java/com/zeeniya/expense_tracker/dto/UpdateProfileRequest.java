package com.zeeniya.expense_tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateProfileRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_.]{4,20}$", message = "Username must be 4 to 20 characters: letters, numbers, _ and .")
    private String username;

    @NotBlank(message = "Name is required")
    private String name;

    public UpdateProfileRequest() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
