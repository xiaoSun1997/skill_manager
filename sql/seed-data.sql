-- ============================================================
-- 种子数据: 默认管理员账号 + 基础角色 + 基础权限
-- ============================================================

-- 插入默认管理员角色
INSERT INTO sys_role (uuid, name, description, status) VALUES
('admin-role-000000000000000001', 'ROLE_ADMIN', '超级管理员', 1),
('user-role-0000000000000000001', 'ROLE_USER', '普通用户', 1);

-- 插入默认管理员用户 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (uuid, username, password, nickname, status) VALUES
('admin-user-000000000000000001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '超级管理员', 1);

-- 关联管理员用户到管理员角色
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- ============================================================
-- 人员权限管理 目录 + 菜单
-- ============================================================

-- 1. 人员权限管理 - 目录
INSERT INTO sys_permission (uuid, name, code, type, parent_id, path, component, icon, route_name, sort_order) VALUES
('perm-sys-dir-0000000000000001', '人员权限管理', 'system:management', 1, NULL, '/system', NULL, 'IconSettings', 'System', 1);

-- 2. 用户管理 - 菜单 (parent = 人员权限管理)
INSERT INTO sys_permission (uuid, name, code, type, parent_id, path, component, icon, route_name, sort_order) VALUES
('perm-user-menu-000000000000001', '用户管理', 'system:user', 2,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-sys-dir-0000000000000001'),
 '/system/user', 'system/user/UserList', 'IconUser', 'UserList', 1);

-- 3. 用户管理 - 按钮权限
INSERT INTO sys_permission (uuid, name, code, type, parent_id, sort_order) VALUES
('perm-user-add-0000000000000001', '新增用户', 'system:user:add', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-user-menu-000000000000001'), NULL),
('perm-user-edit-0000000000000001', '编辑用户', 'system:user:edit', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-user-menu-000000000000001'), NULL),
('perm-user-del-0000000000000001', '删除用户', 'system:user:delete', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-user-menu-000000000000001'), NULL),
('perm-user-list-0000000000000001', '查询用户', 'system:user:list', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-user-menu-000000000000001'), NULL);

-- 4. 角色管理 - 菜单 (parent = 人员权限管理)
INSERT INTO sys_permission (uuid, name, code, type, parent_id, path, component, icon, route_name, sort_order) VALUES
('perm-role-menu-000000000000001', '角色管理', 'system:role', 2,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-sys-dir-0000000000000001'),
 '/system/role', 'system/role/RoleList', 'IconSafe', 'RoleList', 2);

-- 5. 角色管理 - 按钮权限
INSERT INTO sys_permission (uuid, name, code, type, parent_id, sort_order) VALUES
('perm-role-add-0000000000000001', '新增角色', 'system:role:add', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-role-menu-000000000000001'), NULL),
('perm-role-edit-0000000000000001', '编辑角色', 'system:role:edit', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-role-menu-000000000000001'), NULL),
('perm-role-del-0000000000000001', '删除角色', 'system:role:delete', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-role-menu-000000000000001'), NULL),
('perm-role-list-0000000000000001', '查询角色', 'system:role:list', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-role-menu-000000000000001'), NULL),
('perm-role-perm-000000000000001', '分配权限', 'system:role:assign', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-role-menu-000000000000001'), NULL);

-- 6. 权限管理 - 菜单 (parent = 人员权限管理)
INSERT INTO sys_permission (uuid, name, code, type, parent_id, path, component, icon, route_name, sort_order) VALUES
('perm-perm-menu-000000000000001', '权限字典', 'system:permission', 2,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-sys-dir-0000000000000001'),
 '/system/permission', 'system/permission/PermissionList', 'IconMenu', 'PermissionList', 3);

-- 7. 权限管理 - 按钮权限
INSERT INTO sys_permission (uuid, name, code, type, parent_id, sort_order) VALUES
('perm-perm-add-0000000000000001', '新增权限', 'system:permission:add', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-perm-menu-000000000000001'), NULL),
('perm-perm-edit-0000000000000001', '编辑权限', 'system:permission:edit', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-perm-menu-000000000000001'), NULL),
('perm-perm-del-0000000000000001', '删除权限', 'system:permission:delete', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-perm-menu-000000000000001'), NULL),
('perm-perm-list-0000000000000001', '查询权限', 'system:permission:list', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-perm-menu-000000000000001'), NULL);

-- ============================================================
-- 技能管理 目录 + 菜单
-- ============================================================

-- 8. 技能管理 - 目录
INSERT INTO sys_permission (uuid, name, code, type, parent_id, path, component, icon, route_name, sort_order) VALUES
('perm-skill-dir-000000000000001', '技能管理', 'skill:management', 1, NULL, '/skill', NULL, 'IconCode', 'Skill', 10);

-- 9. 技能组管理 - 菜单
INSERT INTO sys_permission (uuid, name, code, type, parent_id, path, component, icon, route_name, sort_order) VALUES
('perm-skill-group-menu-00001', '技能组管理', 'skill:group', 2,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-skill-dir-000000000000001'),
 '/skill/group', 'skill/SkillGroupList', 'IconFolder', 'SkillGroupList', 1);

-- 10. 技能组管理 - 按钮权限
INSERT INTO sys_permission (uuid, name, code, type, parent_id, sort_order) VALUES
('perm-skill-group-list-00001', '查询技能组', 'skill:group:list', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-skill-group-menu-00001'), NULL),
('perm-skill-group-add-000001', '新增技能组', 'skill:group:add', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-skill-group-menu-00001'), NULL),
('perm-skill-group-edit-00001', '编辑技能组', 'skill:group:edit', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-skill-group-menu-00001'), NULL),
('perm-skill-group-delete-0001', '删除技能组', 'skill:group:delete', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-skill-group-menu-00001'), NULL),
('perm-skill-file-upload-00001', '上传文件', 'skill:group:file:upload', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-skill-group-menu-00001'), NULL),
('perm-skill-file-download-001', '下载文件', 'skill:group:file:download', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-skill-group-menu-00001'), NULL),
('perm-skill-file-delete-00001', '删除文件', 'skill:group:file:delete', 3,
 (SELECT id FROM sys_permission WHERE uuid = 'perm-skill-group-menu-00001'), NULL);

-- ============================================================
-- 为管理员角色分配所有权限
-- ============================================================
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM sys_role WHERE name = 'ROLE_ADMIN'),
    id
FROM sys_permission;
