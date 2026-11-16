package com.zeeniya.expense_tracker.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserSignUpRequest {

    @Email(message = "Enter in correct email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_.]{4,20}$", message = "Username must be 4 to 20 characters: letters, numbers, _ and .")
    private String username;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$", message = "Password must be at least 8 characters with a number and a special character")
    private String password;

    public UserSignUpRequest(){}

    public UserSignUpRequest(String email, String username, String name, String password) {
        this.email = email;
        this.username = username;
        this.name = name;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
