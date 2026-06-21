package com.skill.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skill.common.exception.BusinessException;
import com.skill.common.util.UuidUtils;
import com.skill.system.entity.Permission;
import com.skill.system.mapper.PermissionMapper;
import com.skill.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public List<Permission> getAll() {
        return permissionMapper.selectList(null);
    }

    @Override
    public Permission getByUuid(String uuid) {
        Permission p = permissionMapper.selectOne(
                new LambdaQueryWrapper<Permission>().eq(Permission::getUuid, uuid));
        if (p == null) throw new BusinessException("权限不存在");
        return p;
    }

    @Override
    public List<Map<String, Object>> getTree() {
        List<Permission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>().orderByAsc(Permission::getSortOrder));
        Map<Long, List<Permission>> childrenMap = all.stream()
                .filter(p -> p.getParentId() != null)
                .collect(Collectors.groupingBy(Permission::getParentId));

        List<Map<String, Object>> tree = new ArrayList<>();
        for (Permission p : all) {
            if (p.getParentId() == null) {
                tree.add(buildNode(p, childrenMap));
            }
        }
        return tree;
    }

    private Map<String, Object> buildNode(Permission p, Map<Long, List<Permission>> childrenMap) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("uuid", p.getUuid());
        node.put("name", p.getName());
        node.put("code", p.getCode());
        node.put("type", p.getType());
        node.put("parentId", p.getParentId());
        node.put("path", p.getPath());
        node.put("component", p.getComponent());
        node.put("icon", p.getIcon());
        node.put("routeName", p.getRouteName());
        node.put("sortOrder", p.getSortOrder());
        node.put("status", p.getStatus());
        node.put("keepAlive", p.getKeepAlive());
        node.put("hidden", p.getHidden());

        List<Permission> children = childrenMap.getOrDefault(p.getId(), Collections.emptyList());
        if (!children.isEmpty()) {
            node.put("children", children.stream()
                    .map(c -> buildNode(c, childrenMap))
                    .collect(Collectors.toList()));
        }
        return node;
    }

    @Override
    public void create(Permission permission) {
        permission.setUuid(UuidUtils.generate());
        permissionMapper.insert(permission);
    }

    @Override
    public void updateByUuid(String uuid, Permission permission) {
        Permission existing = getByUuid(uuid);
        permission.setId(existing.getId());
        permission.setUuid(null);
        permissionMapper.updateById(permission);
    }

    @Override
    public void deleteByUuid(String uuid) {
        Permission p = getByUuid(uuid);
        // 检查是否有子节点
        Long count = permissionMapper.selectCount(
                new LambdaQueryWrapper<Permission>().eq(Permission::getParentId, p.getId()));
        if (count > 0) {
            throw new BusinessException("该权限下有子权限，请先删除子权限");
        }
        permissionMapper.deleteById(p.getId());
    }
}
