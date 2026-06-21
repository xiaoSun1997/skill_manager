package com.skill.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skill.common.exception.BusinessException;
import com.skill.common.util.UuidUtils;
import com.skill.skill.entity.SkillGroup;
import com.skill.skill.mapper.SkillGroupMapper;
import com.skill.skill.service.SkillGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SkillGroupServiceImpl implements SkillGroupService {

    private final SkillGroupMapper skillGroupMapper;

    @Override
    public Page<SkillGroup> getPage(int pageNum, int pageSize, String keyword) {
        LambdaQueryWrapper<SkillGroup> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SkillGroup::getName, keyword)
                   .or().like(SkillGroup::getDescription, keyword);
        }
        wrapper.orderByDesc(SkillGroup::getCreatedAt);
        return skillGroupMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public SkillGroup getByUuid(String uuid) {
        SkillGroup group = skillGroupMapper.selectOne(
                new LambdaQueryWrapper<SkillGroup>().eq(SkillGroup::getUuid, uuid));
        if (group == null) {
            throw new BusinessException("技能组不存在");
        }
        return group;
    }

    @Override
    public SkillGroup getByName(String name) {
        return skillGroupMapper.selectOne(
                new LambdaQueryWrapper<SkillGroup>().eq(SkillGroup::getName, name));
    }

    @Override
    public void create(SkillGroup group) {
        if (getByName(group.getName()) != null) {
            throw new BusinessException("技能组名称已存在");
        }
        group.setUuid(UuidUtils.generate());
        skillGroupMapper.insert(group);
    }

    @Override
    public void updateByUuid(String uuid, SkillGroup group) {
        SkillGroup existing = getByUuid(uuid);
        // 检查名称是否与其他记录重复
        SkillGroup nameCheck = getByName(group.getName());
        if (nameCheck != null && !nameCheck.getId().equals(existing.getId())) {
            throw new BusinessException("技能组名称已存在");
        }
        group.setId(existing.getId());
        group.setUuid(null); // 不允许修改uuid
        skillGroupMapper.updateById(group);
    }

    @Override
    public void deleteByUuid(String uuid) {
        SkillGroup group = getByUuid(uuid);
        skillGroupMapper.deleteById(group.getId());
    }
}
