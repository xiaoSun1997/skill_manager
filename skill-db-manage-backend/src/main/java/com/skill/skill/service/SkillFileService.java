package com.skill.skill.service;

import com.skill.skill.entity.SkillFile;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.List;

public interface SkillFileService {
    List<SkillFile> listByGroupAndFolder(String groupUuid, String folderName);
    List<SkillFile> listByGroup(String groupUuid);
    SkillFile getByUuid(String fileUuid);
    SkillFile upload(String groupUuid, String folderName, MultipartFile file);
    InputStream download(String fileUuid);
    void deleteByUuid(String fileUuid);
    InputStream downloadGroupAsZip(String groupUuid);
}
