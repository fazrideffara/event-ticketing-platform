package com.fintech.ticket.service.impl;

import com.fintech.ticket.common.constant.SecurityConstant;
import com.fintech.ticket.dto.request.LoginRequest;
import com.fintech.ticket.dto.request.RegisterRequest;
import com.fintech.ticket.dto.response.AuthResponse;
import com.fintech.ticket.entity.Role;
import com.fintech.ticket.entity.User;
import com.fintech.ticket.exception.BadRequestException;
import com.fintech.ticket.repository.UserRepository;
import com.fintech.ticket.security.JwtTokenProvider;
import com.fintech.ticket.service.AuditService;
import com.fintech.ticket.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuditService auditService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        auditService.recordAudit("USER_REGISTERED", "User", savedUser.getId().toString(), savedUser.getUsername(), null, savedUser.getEmail(), null);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                savedUser.getUsername(),
                null,
                null
        );

        String token = tokenProvider.generateToken(authentication, savedUser);

        log.info("New user registered successfully: {}", savedUser.getUsername());

        return buildAuthResponse(savedUser, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = tokenProvider.generateToken(authentication, user);

        log.info("User logged in successfully: {}", user.getUsername());

        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .tokenType(SecurityConstant.TOKEN_PREFIX.trim())
                .expiresInMs(tokenProvider.getExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}
