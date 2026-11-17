package com.fintech.ticket.service;

import com.fintech.ticket.dto.request.LoginRequest;
import com.fintech.ticket.dto.request.RegisterRequest;
import com.fintech.ticket.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
