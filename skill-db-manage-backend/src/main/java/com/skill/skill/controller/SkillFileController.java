package com.skill.skill.controller;

import com.skill.common.dto.Result;
import com.skill.skill.entity.SkillFile;
import com.skill.skill.service.SkillFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/skill/groups/{groupUuid}/files")
@RequiredArgsConstructor
public class SkillFileController {

    private final SkillFileService skillFileService;

    @GetMapping
    @PreAuthorize("hasAuthority('skill:group:list')")
    public Result<List<SkillFile>> listFiles(
            @PathVariable String groupUuid,
            @RequestParam(required = false) String folder) {
        if (folder != null && !folder.isEmpty()) {
            return Result.success(skillFileService.listByGroupAndFolder(groupUuid, folder));
        }
        return Result.success(skillFileService.listByGroup(groupUuid));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('skill:group:file:upload')")
    public Result<SkillFile> upload(
            @PathVariable String groupUuid,
            @RequestParam String folderName,
            @RequestParam MultipartFile file) {
        SkillFile skillFile = skillFileService.upload(groupUuid, folderName, file);
        return Result.success(skillFile);
    }

    @GetMapping("/{fileUuid}")
    @PreAuthorize("hasAuthority('skill:group:file:download')")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable String groupUuid,
            @PathVariable String fileUuid) {
        SkillFile file = skillFileService.getByUuid(fileUuid);
        InputStream inputStream = skillFileService.download(fileUuid);

        String filename = URLEncoder.encode(file.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/{fileUuid}")
    @PreAuthorize("hasAuthority('skill:group:file:delete')")
    public Result<Void> delete(
            @PathVariable String groupUuid,
            @PathVariable String fileUuid) {
        skillFileService.deleteByUuid(fileUuid);
        return Result.success(null);
    }
}
