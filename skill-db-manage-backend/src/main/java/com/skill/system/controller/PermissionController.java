package com.skill.system.controller;

import com.skill.common.dto.Result;
import com.skill.system.entity.Permission;
import com.skill.system.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public Result<List<Map<String, Object>>> getTree() {
        return Result.success(permissionService.getTree());
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public Result<Permission> getByUuid(@PathVariable String uuid) {
        return Result.success(permissionService.getByUuid(uuid));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:permission:add')")
    public Result<Void> create(@RequestBody Permission permission) {
        permissionService.create(permission);
        return Result.success();
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:permission:edit')")
    public Result<Void> update(@PathVariable String uuid, @RequestBody Permission permission) {
        permissionService.updateByUuid(uuid, permission);
        return Result.success();
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    public Result<Void> delete(@PathVariable String uuid) {
        permissionService.deleteByUuid(uuid);
        return Result.success();
    }
}
