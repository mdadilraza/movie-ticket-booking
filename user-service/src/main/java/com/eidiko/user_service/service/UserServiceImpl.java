package com.eidiko.user_service.service;

import com.eidiko.user_service.dto.AuthRequest;
import com.eidiko.user_service.dto.AuthResponse;
import com.eidiko.user_service.dto.UserRequest;
import com.eidiko.user_service.entity.RefreshToken;
import com.eidiko.user_service.entity.User;
import com.eidiko.user_service.exception.UserAlreadyExistException;
import com.eidiko.user_service.exception.UserNotFoundException;
import com.eidiko.user_service.repository.UserRepository;
import com.eidiko.user_service.security.CustomUserDetailsService;
import com.eidiko.user_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public User register(UserRequest request) {
        log.info("UserRequest {}", request);
//        if (userRepository.existsByUsername(request.getUsername()) ||
//                userRepository.existsByEmail(request.getEmail())) {
//            throw new UserAlreadyExistException("Username or email already exists");
//        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole() != null ? request.getRole() : "USER");
        log.info("User {}", user);
        return userRepository.save(user);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        if (authentication.isAuthenticated()) {
            log.info("true");
            String username = request.getUsername();
            User user = (User) customUserDetailsService.loadUserByUsername(username);
            log.info(String.valueOf(user));
            String accessToken = jwtUtil.generateAccessToken(username, user.getRole());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(username);

            return new AuthResponse(accessToken, refreshToken.getToken());
        }
        return new AuthResponse();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        refreshTokenService.verifyExpiration(tokenEntity);
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(username, user.getRole());
        return new AuthResponse(newAccessToken, refreshToken);
    }

    @Override
    public Map<String, String> validateToken(String token) {
        if (jwtUtil.validateToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("username", jwtUtil.extractUsername(token));
            response.put("role", jwtUtil.extractRole(token));
            return response;
        }
        throw new IllegalArgumentException("Invalid token");
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }


}
