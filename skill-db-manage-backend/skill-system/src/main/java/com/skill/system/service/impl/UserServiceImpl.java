package com.skill.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skill.common.exception.BusinessException;
import com.skill.common.util.UuidUtils;
import com.skill.system.entity.Role;
import com.skill.system.entity.User;
import com.skill.system.mapper.RoleMapper;
import com.skill.system.mapper.UserMapper;
import com.skill.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> getPage(int pageNum, int pageSize, String keyword, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public User getByUuid(String uuid) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUuid, uuid));
        if (user == null) throw new BusinessException("用户不存在");
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public User getByPhone(String phone) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    public void create(User user) {
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        user.setUuid(UuidUtils.generate());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    @Override
    public void updateByUuid(String uuid, User user) {
        User existing = getByUuid(uuid);
        user.setId(existing.getId());
        user.setUuid(null); // 不允许修改uuid
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
    }

    @Override
    public void deleteByUuid(String uuid) {
        User user = getByUuid(uuid);
        userMapper.deleteById(user.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(String userUuid, List<String> roleUuids) {
        User user = getByUuid(userUuid);
        // 删除原有角色关联
        userMapper.deleteUserRoleByUserId(user.getId());
        // 添加新角色关联
        if (roleUuids != null && !roleUuids.isEmpty()) {
            List<Role> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<Role>().in(Role::getUuid, roleUuids));
            for (Role role : roles) {
                userMapper.insertUserRole(user.getId(), role.getId());
            }
        }
    }

    @Override
    public List<String> getRoleUuids(String userUuid) {
        User user = getByUuid(userUuid);
        return userMapper.selectRoleUuidsByUserId(user.getId());
    }

    @Override
    public List<Map<String, Object>> getRoleList(String userUuid) {
        User user = getByUuid(userUuid);
        List<String> assignedUuids = userMapper.selectRoleUuidsByUserId(user.getId());
        List<Role> allRoles = roleMapper.selectList(null);

        List<Map<String, Object>> result = new ArrayList<>();
        for (Role role : allRoles) {
            Map<String, Object> item = new HashMap<>();
            item.put("uuid", role.getUuid());
            item.put("name", role.getName());
            item.put("description", role.getDescription());
            item.put("assigned", assignedUuids.contains(role.getUuid()));
            result.add(item);
        }
        return result;
    }
}
