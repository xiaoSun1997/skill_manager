package com.skill.skill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("skill_group")
public class SkillGroup {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String uuid;
    private String name;
    private String description;
    private String folderNames;
    private Long ownerId;
    @TableLogic
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
