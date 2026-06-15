package com.skill.auth.service;

import com.skill.auth.model.LoginRequest;
import com.skill.auth.model.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse smsLogin(String phone, String code);
    void sendSmsCode(String phone);
    void logout(String token);
    LoginResponse refreshToken(String refreshToken);
}
