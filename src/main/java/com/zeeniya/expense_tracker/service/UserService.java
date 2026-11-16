package com.zeeniya.expense_tracker.service;

import com.zeeniya.expense_tracker.dto.AuthResponse;
import com.zeeniya.expense_tracker.dto.UserLogInRequest;
import com.zeeniya.expense_tracker.dto.UserResponse;
import com.zeeniya.expense_tracker.dto.UserSignUpRequest;
import com.zeeniya.expense_tracker.entity.User;
import com.zeeniya.expense_tracker.entity.Role;
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

    private AuthResponse toAuthResponse(User user){
        UserResponse userResponse = new UserResponse(user.getId(), user.getEmail(), user.getDisplayUsername(), user.getName());
        return new AuthResponse(jwtUtil.generateToken(user), userResponse);
    }


}
