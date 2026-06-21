package com.skill.auth.controller;

import com.skill.auth.model.LoginRequest;
import com.skill.auth.model.LoginResponse;
import com.skill.auth.model.SmsLoginRequest;
import com.skill.auth.service.AuthService;
import com.skill.common.dto.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/sms/login")
    public Result<LoginResponse> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        LoginResponse response = authService.smsLogin(request.getPhone(), request.getCode());
        return Result.success(response);
    }

    @PostMapping("/sms/send")
    public Result<Void> sendSms(@RequestBody Map<String, String> body) {
        authService.sendSmsCode(body.get("phone"));
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        authService.logout(token);
        return Result.success();
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        LoginResponse response = authService.refreshToken(body.get("refreshToken"));
        return Result.success(response);
    }
}
