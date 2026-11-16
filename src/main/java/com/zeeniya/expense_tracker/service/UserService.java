package com.zeeniya.expense_tracker.service;

import com.zeeniya.expense_tracker.dto.AuthResponse;
import com.zeeniya.expense_tracker.dto.UpdateProfileRequest;
import com.zeeniya.expense_tracker.dto.UserLogInRequest;
import com.zeeniya.expense_tracker.dto.UserResponse;
import com.zeeniya.expense_tracker.dto.UserSignUpRequest;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.entity.Role;
import com.zeeniya.expense_tracker.exception.DuplicateResourceException;
import com.zeeniya.expense_tracker.repository.UserRepository;
import com.zeeniya.expense_tracker.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse signUp(UserSignUpRequest request){
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists.");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("This username is already taken.");
        }

        String password = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getEmail(), request.getUsername(), request.getName(), password, Role.USER);
        User savedUser =  userRepository.save(user);

        return toAuthResponse(savedUser);
    }

    public AuthResponse logIn(UserLogInRequest request){
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        Authentication result = authenticationManager.authenticate(authRequest);
        User user = (User) result.getPrincipal();

        return toAuthResponse(user);
    }

    public UserResponse getProfile(User user){
        return toUserResponse(user);
    }

    public UserResponse updateProfile(User user, UpdateProfileRequest request){
        if (userRepository.existsByUsernameIgnoreCaseAndIdNot(request.getUsername(), user.getId())) {
            throw new DuplicateResourceException("This username is already taken.");
        }
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        return toUserResponse(userRepository.save(user));
    }

    private UserResponse toUserResponse(User user){
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayUsername(), user.getName());
    }

    private AuthResponse toAuthResponse(User user){
        return new AuthResponse(jwtUtil.generateToken(user), toUserResponse(user));
    }


}
