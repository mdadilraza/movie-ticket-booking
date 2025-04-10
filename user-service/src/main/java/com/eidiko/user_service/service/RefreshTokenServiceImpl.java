package com.eidiko.user_service.service;

import com.eidiko.user_service.entity.RefreshToken;
import com.eidiko.user_service.entity.User;
import com.eidiko.user_service.exception.UserNotFoundException;
import com.eidiko.user_service.repository.RefreshTokenRepository;
import com.eidiko.user_service.repository.UserRepository;
import com.eidiko.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService{

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(jwtUtil.generateRefreshToken(username));
        refreshToken.setExpiryDate(jwtUtil.getExpirationDateFromToken(refreshToken.getToken()));
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new IllegalArgumentException("Refresh token has expired");
        }
    }
}
