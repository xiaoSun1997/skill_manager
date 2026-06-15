package com.skill.system.service;

import com.skill.system.entity.Role;

import java.util.List;
import java.util.Map;

public interface RoleService {
    List<Role> getAll();
    Role getByUuid(String uuid);
    void create(Role role);
    void updateByUuid(String uuid, Role role);
    void deleteByUuid(String uuid);
    void assignPermissions(String roleUuid, List<String> permissionUuids);
    List<String> getPermissionUuids(String roleUuid);
    List<Map<String, Object>> getPermissionTree(String roleUuid);
}
