package com.skill.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skill.system.entity.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    Page<User> getPage(int pageNum, int pageSize, String keyword, Integer status);
    User getByUuid(String uuid);
    User getByUsername(String username);
    User getByPhone(String phone);
    void create(User user);
    void updateByUuid(String uuid, User user);
    void deleteByUuid(String uuid);
    void assignRoles(String userUuid, List<String> roleUuids);
    List<String> getRoleUuids(String userUuid);
    List<Map<String, Object>> getRoleList(String userUuid);
}
