package com.skill.system.controller;

import com.skill.common.dto.PageResult;
import com.skill.common.dto.Result;
import com.skill.system.entity.User;
import com.skill.system.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<PageResult<User>> getPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<User> page = userService.getPage(pageNum, pageSize, keyword, status);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<User> getByUuid(@PathVariable String uuid) {
        return Result.success(userService.getByUuid(uuid));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public Result<Void> create(@RequestBody User user) {
        userService.create(user);
        return Result.success();
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> update(@PathVariable String uuid, @RequestBody User user) {
        userService.updateByUuid(uuid, user);
        return Result.success();
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public Result<Void> delete(@PathVariable String uuid) {
        userService.deleteByUuid(uuid);
        return Result.success();
    }

    @GetMapping("/{uuid}/roles")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<List<Map<String, Object>>> getRoles(@PathVariable String uuid) {
        return Result.success(userService.getRoleList(uuid));
    }

    @PutMapping("/{uuid}/roles")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public Result<Void> assignRoles(@PathVariable String uuid, @RequestBody Map<String, List<String>> body) {
        userService.assignRoles(uuid, body.get("roleUuids"));
        return Result.success();
    }
}
