package com.skill.skill.controller;

import com.skill.common.dto.Result;
import com.skill.common.util.JwtTokenProvider;
import com.skill.system.entity.User;
import com.skill.system.mapper.RoleMapper;
import com.skill.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cli")
@RequiredArgsConstructor
public class CliAuthController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/auth/token")
    public Result<Map<String, Object>> getCliToken(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return Result.error("用户名和密码不能为空");
        }

        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        List<String> roleNames = userMapper.selectRoleNamesByUserId(user.getId());

        // 生成短期 token（24小时）
        String token = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roleNames);

        return Result.success(Map.of(
                "accessToken", token,
                "expiresIn", 86400,
                "userInfo", Map.of(
                        "uuid", user.getUuid(),
                        "username", user.getUsername(),
                        "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername()
                )
        ));
    }
}
