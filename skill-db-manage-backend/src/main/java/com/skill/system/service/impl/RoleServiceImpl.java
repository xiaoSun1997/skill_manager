package com.skill.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skill.common.exception.BusinessException;
import com.skill.common.util.UuidUtils;
import com.skill.system.entity.Permission;
import com.skill.system.entity.Role;
import com.skill.system.mapper.PermissionMapper;
import com.skill.system.mapper.RoleMapper;
import com.skill.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public List<Role> getAll() {
        return roleMapper.selectList(null);
    }

    @Override
    public Role getByUuid(String uuid) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getUuid, uuid));
        if (role == null) throw new BusinessException("角色不存在");
        return role;
    }

    @Override
    public void create(Role role) {
        role.setUuid(UuidUtils.generate());
        if (roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getName, role.getName())) != null) {
            throw new BusinessException("角色名称已存在");
        }
        roleMapper.insert(role);
    }

    @Override
    public void updateByUuid(String uuid, Role role) {
        Role existing = getByUuid(uuid);
        role.setId(existing.getId());
        role.setUuid(null);
        roleMapper.updateById(role);
    }

    @Override
    public void deleteByUuid(String uuid) {
        Role role = getByUuid(uuid);
        roleMapper.deleteById(role.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(String roleUuid, List<String> permissionUuids) {
        Role role = getByUuid(roleUuid);
        // 删除原有权限关联
        roleMapper.deleteRolePermissionsByRoleId(role.getId());
        // 添加新权限关联
        if (permissionUuids != null && !permissionUuids.isEmpty()) {
            List<Permission> permissions = permissionMapper.selectList(
                    new LambdaQueryWrapper<Permission>().in(Permission::getUuid, permissionUuids));
            for (Permission permission : permissions) {
                roleMapper.insertRolePermission(role.getId(), permission.getId());
            }
        }
    }

    @Override
    public List<String> getPermissionUuids(String roleUuid) {
        Role role = getByUuid(roleUuid);
        return roleMapper.selectPermissionUuidsByRoleId(role.getId());
    }

    @Override
    public List<Map<String, Object>> getPermissionTree(String roleUuid) {
        List<Permission> allPermissions = permissionMapper.selectList(null);
        List<String> assignedUuids = roleUuid != null ? getPermissionUuids(roleUuid) : Collections.emptyList();

        // 构建树形结构
        List<Map<String, Object>> tree = new ArrayList<>();
        Map<Long, List<Permission>> childrenMap = allPermissions.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        for (Permission p : allPermissions) {
            if (p.getParentId() == null) {
                tree.add(buildPermissionNode(p, childrenMap, assignedUuids));
            }
        }

        // 排序
        tree.sort(Comparator.comparingInt(a -> (Integer) a.getOrDefault("sortOrder", 0)));
        return tree;
    }

    private Map<String, Object> buildPermissionNode(Permission p, Map<Long, List<Permission>> childrenMap,
                                                    List<String> assignedUuids) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("uuid", p.getUuid());
        node.put("name", p.getName());
        node.put("code", p.getCode());
        node.put("type", p.getType());
        node.put("sortOrder", p.getSortOrder());
        node.put("assigned", assignedUuids.contains(p.getUuid()));

        List<Permission> children = childrenMap.getOrDefault(p.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<Map<String, Object>> childNodes = children.stream()
                    .map(c -> buildPermissionNode(c, childrenMap, assignedUuids))
                    .sorted(Comparator.comparingInt(a -> (Integer) a.getOrDefault("sortOrder", 0)))
                    .collect(Collectors.toList());
            node.put("children", childNodes);
        }

        return node;
    }
}
