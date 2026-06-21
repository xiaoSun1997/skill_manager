package com.skill.auth.service.impl;

import com.skill.auth.model.LoginRequest;
import com.skill.auth.model.LoginResponse;
import com.skill.auth.service.AuthService;
import com.skill.common.exception.BusinessException;
import com.skill.common.util.JwtTokenProvider;
import com.skill.system.entity.LoginLog;
import com.skill.system.entity.Permission;
import com.skill.system.entity.User;
import com.skill.system.mapper.RoleMapper;
import com.skill.system.mapper.UserMapper;
import com.skill.system.service.LoginLogService;
import com.skill.system.service.PermissionService;
import com.skill.system.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LoginLogService loginLogService;
    private final HttpServletRequest request;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            saveLoginLog(null, loginRequest.getUsername(), 1, 0, "用户不存在");
            throw new BadCredentialsException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            saveLoginLog(user.getId(), user.getUsername(), 1, 0, "账号已被禁用");
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            saveLoginLog(user.getId(), user.getUsername(), 1, 0, "密码错误");
            throw new BadCredentialsException("用户名或密码错误");
        }

        saveLoginLog(user.getId(), user.getUsername(), 1, 1, null);
        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse smsLogin(String phone, String code) {
        // TODO: 对接阿里云短信验证码服务
        // 模拟验证码校验: 固定验证码 123456
        if (!"123456".equals(code)) {
            saveLoginLog(null, phone, 2, 0, "验证码错误");
            throw new BusinessException("验证码错误");
        }

        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            saveLoginLog(null, phone, 2, 0, "手机号未注册");
            throw new BusinessException("手机号未注册");
        }

        saveLoginLog(user.getId(), user.getUsername(), 2, 1, null);
        return buildLoginResponse(user);
    }

    @Override
    public void sendSmsCode(String phone) {
        // TODO: 对接阿里云短信服务发送验证码
        // 模拟发送成功
        redisTemplate.opsForValue().set("sms:code:" + phone, "123456", 5, TimeUnit.MINUTES);
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            // 将token加入黑名单，过期时间等于token剩余有效期
            redisTemplate.opsForValue().set("token:blacklist:" + token, "1", 2, TimeUnit.HOURS);
        }
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Refresh Token 无效或已过期");
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        List<String> roleNames = userMapper.selectRoleNamesByUserId(user.getId());
        List<String> roleUuids = userMapper.selectRoleUuidsByUserId(user.getId());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roleNames);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // 获取权限标识
        List<String> permissions = roleMapper.selectPermissionCodesByUserId(user.getId());

        // 获取菜单树
        List<Permission> allPerms = permissionService.getAll();
        Set<Long> userPermIds = new HashSet<>();
        for (String roleUuid : roleUuids) {
            List<Long> permIds = roleMapper.selectPermissionIdsByRoleId(
                    roleService.getByUuid(roleUuid).getId());
            userPermIds.addAll(permIds);
        }

        List<Permission> menuPerms = allPerms.stream()
                .filter(p -> p.getType() <= 2 && userPermIds.contains(p.getId()))
                .sorted(Comparator.comparingInt(Permission::getSortOrder))
                .collect(Collectors.toList());

        Map<Long, List<Permission>> childrenMap = menuPerms.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        List<Map<String, Object>> menus = new ArrayList<>();
        for (Permission p : menuPerms) {
            if (p.getParentId() == null) {
                menus.add(buildMenuNode(p, childrenMap));
            }
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(7200)
                .userInfo(Map.of(
                        "uuid", user.getUuid(),
                        "username", user.getUsername(),
                        "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername(),
                        "avatar", user.getAvatar() != null ? user.getAvatar() : ""
                ))
                .menus(menus)
                .permissions(permissions)
                .build();
    }

    private Map<String, Object> buildMenuNode(Permission p, Map<Long, List<Permission>> childrenMap) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("uuid", p.getUuid());
        node.put("name", p.getName());
        node.put("type", p.getType());
        if (p.getType() == 2) {
            node.put("path", p.getPath());
            node.put("routeName", p.getRouteName());
            node.put("component", p.getComponent());
            node.put("keepAlive", p.getKeepAlive());
        }
        node.put("icon", p.getIcon());
        node.put("sortOrder", p.getSortOrder());
        node.put("hidden", p.getHidden());

        List<Permission> children = childrenMap.getOrDefault(p.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            node.put("children", children.stream()
                    .map(c -> buildMenuNode(c, childrenMap))
                    .sorted(Comparator.comparingInt(a -> (Integer) a.getOrDefault("sortOrder", 0)))
                    .collect(Collectors.toList()));
        }
        return node;
    }

    private void saveLoginLog(Long userId, String username, Integer loginType, Integer status, String failReason) {
        try {
            LoginLog log = new LoginLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setLoginType(loginType);
            log.setIp(request.getRemoteAddr());
            log.setUserAgent(request.getHeader("User-Agent"));
            log.setStatus(status);
            log.setFailReason(failReason);
            loginLogService.save(log);
        } catch (Exception ignored) {
            // 登录日志不影响主流程
        }
    }
}
