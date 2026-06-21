package com.skill.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.skill.system.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT p.uuid FROM sys_role_permission rp " +
            "JOIN sys_permission p ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId}")
    List<String> selectPermissionUuidsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT p.id FROM sys_role_permission rp " +
            "JOIN sys_permission p ON rp.permission_id = p.id " +
            "WHERE rp.role_id = #{roleId}")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT p.code FROM sys_role_permission rp " +
            "JOIN sys_permission p ON rp.permission_id = p.id " +
            "JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    void deleteRolePermissionsByRoleId(@Param("roleId") Long roleId);

    @Insert("INSERT INTO sys_role_permission(role_id, permission_id) VALUES(#{roleId}, #{permissionId})")
    void insertRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);
}
