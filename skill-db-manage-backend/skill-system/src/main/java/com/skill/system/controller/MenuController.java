package com.skill.system.controller;

import com.skill.common.dto.Result;
import com.skill.system.entity.Permission;
import com.skill.system.entity.User;
import com.skill.system.mapper.RoleMapper;
import com.skill.system.mapper.UserMapper;
import com.skill.system.service.PermissionService;
import com.skill.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class MenuController {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionService permissionService;
    private final RoleService roleService;

    @GetMapping("/menus")
    public Result<Map<String, Object>> getMenus(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return Result.unauthorized("未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.unauthorized("用户不存在");
        }

        // 获取用户的角色
        List<String> roleNames = userMapper.selectRoleNamesByUserId(userId);
        List<String> roleUuids = userMapper.selectRoleUuidsByUserId(userId);

        // 获取用户的权限标识
        List<String> permissions = roleMapper.selectPermissionCodesByUserId(userId);

        // 获取用户的菜单树（只返回 type=1,2 的目录和菜单）
        List<Permission> allPerms = permissionService.getAll();
        Set<Long> userPermIds = new HashSet<>();
        for (String roleUuid : roleUuids) {
            List<Long> permIds = roleMapper.selectPermissionIdsByRoleId(
                    roleService.getByUuid(roleUuid).getId());
            userPermIds.addAll(permIds);
        }

        // 构建菜单树
        List<Permission> menuPerms = allPerms.stream()
                .filter(p -> p.getType() <= 2 && userPermIds.contains(p.getId()))
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
        menus.sort(Comparator.comparingInt(a -> (Integer) a.getOrDefault("sortOrder", 0)));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("menus", menus);
        result.put("permissions", permissions);
        result.put("userInfo", Map.of(
                "uuid", user.getUuid(),
                "username", user.getUsername(),
                "nickname", user.getNickname() != null ? user.getNickname() : user.getUsername(),
                "avatar", user.getAvatar() != null ? user.getAvatar() : ""
        ));

        return Result.success(result);
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
}
