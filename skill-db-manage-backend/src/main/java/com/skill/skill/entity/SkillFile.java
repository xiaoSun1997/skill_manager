package com.skill.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("skill_file")
public class SkillFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String uuid;
    private Long groupId;
    private String folderName;
    private String fileName;
    private String ossKey;
    private Long fileSize;
    private String fileType;
    @TableLogic
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
