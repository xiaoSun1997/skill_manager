package com.skill.skill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skill.common.dto.PageResult;
import com.skill.common.dto.Result;
import com.skill.skill.entity.SkillGroup;
import com.skill.skill.service.SkillFileService;
import com.skill.skill.service.SkillGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/skill/groups")
@RequiredArgsConstructor
public class SkillGroupController {

    private final SkillGroupService skillGroupService;
    private final SkillFileService skillFileService;

    @GetMapping
    @PreAuthorize("hasAuthority('skill:group:list')")
    public Result<PageResult<SkillGroup>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        Page<SkillGroup> page = skillGroupService.getPage(pageNum, pageSize, keyword);
        return Result.success(PageResult.of(page.getRecords(), page.getTotal(), pageNum, pageSize));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('skill:group:list')")
    public Result<SkillGroup> getByUuid(@PathVariable String uuid) {
        return Result.success(skillGroupService.getByUuid(uuid));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('skill:group:add')")
    public Result<SkillGroup> create(@RequestBody SkillGroup group) {
        skillGroupService.create(group);
        return Result.success(group);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('skill:group:edit')")
    public Result<Void> update(@PathVariable String uuid, @RequestBody SkillGroup group) {
        skillGroupService.updateByUuid(uuid, group);
        return Result.success(null);
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAuthority('skill:group:delete')")
    public Result<Void> delete(@PathVariable String uuid) {
        skillGroupService.deleteByUuid(uuid);
        return Result.success(null);
    }

    @GetMapping("/{uuid}/download")
    @PreAuthorize("hasAuthority('skill:group:file:download')")
    public ResponseEntity<InputStreamResource> downloadGroup(@PathVariable String uuid) {
        SkillGroup group = skillGroupService.getByUuid(uuid);
        InputStream zipStream = skillFileService.downloadGroupAsZip(uuid);

        String filename = URLEncoder.encode(group.getName() + ".zip", StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(zipStream));
    }
}
