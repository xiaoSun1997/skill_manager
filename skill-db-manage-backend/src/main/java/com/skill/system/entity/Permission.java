package com.skill.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String uuid;
    private String name;
    private String code;
    private Integer type;
    private Long parentId;
    private String path;
    private String component;
    private String icon;
    private String routeName;
    private Integer keepAlive;
    private Integer hidden;
    private String method;
    private Integer sortOrder;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
