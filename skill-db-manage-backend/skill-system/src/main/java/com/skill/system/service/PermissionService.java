package com.skill.system.service;

import com.skill.system.entity.Permission;

import java.util.List;
import java.util.Map;

public interface PermissionService {
    List<Permission> getAll();
    Permission getByUuid(String uuid);
    List<Map<String, Object>> getTree();
    void create(Permission permission);
    void updateByUuid(String uuid, Permission permission);
    void deleteByUuid(String uuid);
}
