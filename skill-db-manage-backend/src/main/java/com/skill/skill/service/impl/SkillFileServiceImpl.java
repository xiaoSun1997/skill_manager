package com.skill.skill.service.impl;

import com.skill.common.exception.BusinessException;
import com.skill.common.util.UuidUtils;
import com.skill.skill.entity.SkillFile;
import com.skill.skill.entity.SkillGroup;
import com.skill.skill.mapper.SkillFileMapper;
import com.skill.skill.service.OssService;
import com.skill.skill.service.SkillFileService;
import com.skill.skill.service.SkillGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillFileServiceImpl implements SkillFileService {

    private final SkillGroupService skillGroupService;
    private final SkillFileMapper skillFileMapper;
    private final OssService ossService;

    @Override
    public List<SkillFile> listByGroupAndFolder(String groupUuid, String folderName) {
        SkillGroup group = skillGroupService.getByUuid(groupUuid);
        return skillFileMapper.selectByGroupAndFolder(group.getId(), folderName);
    }

    @Override
    public List<SkillFile> listByGroup(String groupUuid) {
        SkillGroup group = skillGroupService.getByUuid(groupUuid);
        return skillFileMapper.selectByGroupId(group.getId());
    }

    @Override
    public SkillFile getByUuid(String fileUuid) {
        SkillFile file = skillFileMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SkillFile>()
                        .eq(SkillFile::getUuid, fileUuid));
        if (file == null) {
            throw new BusinessException("文件不存在");
        }
        return file;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SkillFile upload(String groupUuid, String folderName, MultipartFile multipartFile) {
        SkillGroup group = skillGroupService.getByUuid(groupUuid);

        try {
            String fileUuid = UuidUtils.generate();
            String originalName = multipartFile.getOriginalFilename();
            String ossKey = String.format("skill-groups/%s/%s/%s-%s",
                    groupUuid, folderName, fileUuid, originalName != null ? originalName : "file");

            ossService.upload(multipartFile.getInputStream(), multipartFile.getSize(),
                    ossKey, multipartFile.getContentType());

            SkillFile skillFile = new SkillFile();
            skillFile.setUuid(fileUuid);
            skillFile.setGroupId(group.getId());
            skillFile.setFolderName(folderName);
            skillFile.setFileName(originalName);
            skillFile.setOssKey(ossKey);
            skillFile.setFileSize(multipartFile.getSize());
            skillFile.setFileType(multipartFile.getContentType());
            skillFileMapper.insert(skillFile);

            log.info("File uploaded: group={}, folder={}, file={}", groupUuid, folderName, originalName);
            return skillFile;
        } catch (IOException e) {
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream download(String fileUuid) {
        SkillFile file = getByUuid(fileUuid);
        return ossService.download(file.getOssKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByUuid(String fileUuid) {
        SkillFile file = getByUuid(fileUuid);
        ossService.delete(file.getOssKey());
        skillFileMapper.deleteById(file.getId());
    }

    @Override
    public InputStream downloadGroupAsZip(String groupUuid) {
        SkillGroup group = skillGroupService.getByUuid(groupUuid);
        List<SkillFile> files = skillFileMapper.selectByGroupId(group.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (SkillFile file : files) {
                try (InputStream is = ossService.download(file.getOssKey())) {
                    ZipEntry entry = new ZipEntry(file.getFolderName() + "/" + file.getFileName());
                    zos.putNextEntry(entry);
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                } catch (Exception e) {
                    log.warn("Failed to add file to zip: {}", file.getFileName(), e);
                }
            }

            zos.finish();
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (IOException e) {
            throw new BusinessException("打包下载失败: " + e.getMessage());
        }
    }
}
