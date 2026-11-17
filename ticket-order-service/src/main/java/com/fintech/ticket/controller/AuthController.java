package com.fintech.ticket.controller;

import com.fintech.ticket.common.constant.ApiUrlConstant;
import com.fintech.ticket.common.constant.AppConstant;
import com.fintech.ticket.common.response.ApiResponse;
import com.fintech.ticket.dto.request.LoginRequest;
import com.fintech.ticket.dto.request.RegisterRequest;
import com.fintech.ticket.dto.response.AuthResponse;
import com.fintech.ticket.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiUrlConstant.AUTH_BASE)
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "Endpoints for user registration and JWT authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping(ApiUrlConstant.AUTH_REGISTER)
    @Operation(summary = "Register a new user", description = "Creates a new user account with default ROLE_USER")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(AppConstant.MSG_REGISTRATION_SUCCESS, response));
    }

    @PostMapping(ApiUrlConstant.AUTH_LOGIN)
    @Operation(summary = "Authenticate user", description = "Validates credentials and returns JWT Bearer token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(AppConstant.MSG_LOGIN_SUCCESS, response));
    }
}
