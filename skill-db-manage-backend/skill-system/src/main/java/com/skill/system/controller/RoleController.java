package com.skill.system.controller;

import com.skill.common.dto.Result;
import com.skill.system.entity.Role;
import com.skill.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<Role>> getAll() {
        return Result.success(roleService.getAll());
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<Role> getByUuid(@PathVariable String uuid) {
        return Result.success(roleService.getByUuid(uuid));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public Result<Void> create(@RequestBody Role role) {
        roleService.create(role);
        return Result.success();
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public Result<Void> update(@PathVariable String uuid, @RequestBody Role role) {
        roleService.updateByUuid(uuid, role);
        return Result.success();
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public Result<Void> delete(@PathVariable String uuid) {
        roleService.deleteByUuid(uuid);
        return Result.success();
    }

    @GetMapping("/{uuid}/permissions")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<String>> getPermissions(@PathVariable String uuid) {
        return Result.success(roleService.getPermissionUuids(uuid));
    }

    @PutMapping("/{uuid}/permissions")
    @PreAuthorize("hasAuthority('system:role:assign')")
    public Result<Void> assignPermissions(@PathVariable String uuid, @RequestBody Map<String, List<String>> body) {
        roleService.assignPermissions(uuid, body.get("permissionUuids"));
        return Result.success();
    }

    @GetMapping("/{uuid}/permission-tree")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<Map<String, Object>>> getPermissionTree(@PathVariable String uuid) {
        return Result.success(roleService.getPermissionTree(uuid));
    }

    @GetMapping("/permission-tree")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<Map<String, Object>>> getAllPermissionTree() {
        return Result.success(roleService.getPermissionTree(null));
    }
}
