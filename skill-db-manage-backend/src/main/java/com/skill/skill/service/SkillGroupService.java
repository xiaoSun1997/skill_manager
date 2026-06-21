package com.skill.skill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skill.skill.entity.SkillGroup;

public interface SkillGroupService {
    Page<SkillGroup> getPage(int pageNum, int pageSize, String keyword);
    SkillGroup getByUuid(String uuid);
    SkillGroup getByName(String name);
    void create(SkillGroup group);
    void updateByUuid(String uuid, SkillGroup group);
    void deleteByUuid(String uuid);
}
