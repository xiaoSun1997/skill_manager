-- ============================================================
-- 种子数据: 默认管理员账号 + 基础角色 + 基础权限
-- ============================================================

-- 插入默认管理员角色
INSERT INTO sys_role (uuid, name, description, status) VALUES
('admin-role-000000000000000001', 'ROLE_ADMIN', '超级管理员', 1),
('user-role-0000000000000000001', 'ROLE_USER', '普通用户', 1);

-- 插入默认管理员用户 (密码: admin123, BCrypt加密)
-- 实际密码需要 BCrypt 加密，这里只做占位
INSERT INTO sys_user (uuid, username, password, nickname, status) VALUES
('admin-user-000000000000000001', 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '超级管理员', 1);

-- 关联管理员用户到管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
((SELECT id FROM sys_user WHERE username = 'admin'), (SELECT id FROM sys_role WHERE name = 'ROLE_ADMIN'));

-- 插入基本权限数据（系统管理目录 → 用户管理/角色管理/权限管理菜单）
INSERT INTO sys_permission (uuid, name, code, type, parent_id, path, component, icon, route_name, sort_order) VALUES

-- 1. 系统管理 - 目录
('perm-sys-dir-0000000000000001', '系统管理', 'system:management', 1, NULL, '/system', NULL, 'IconSettings', 'System', 1),

-- 2. 用户管理 - 菜单 (parent = 系统管理)
('perm-user-menu-000000000000001', '用户管理', 'system:user', 2, 1, '/system/user', 'system/user/UserList', 'IconUser', 'UserList', 1),

-- 3. 用户管理 - 按钮权限
('perm-user-add-0000000000000001', '新增用户', 'system:user:add', 3, 2, NULL, NULL, NULL, NULL, NULL),
('perm-user-edit-0000000000000001', '编辑用户', 'system:user:edit', 3, 2, NULL, NULL, NULL, NULL, NULL),
('perm-user-del-0000000000000001', '删除用户', 'system:user:delete', 3, 2, NULL, NULL, NULL, NULL, NULL),
('perm-user-list-0000000000000001', '查询用户', 'system:user:list', 3, 2, NULL, NULL, NULL, NULL, NULL),

-- 4. 角色管理 - 菜单 (parent = 系统管理)
('perm-role-menu-000000000000001', '角色管理', 'system:role', 2, 1, '/system/role', 'system/role/RoleList', 'IconSafe', 'RoleList', 2),

-- 5. 角色管理 - 按钮权限
('perm-role-add-0000000000000001', '新增角色', 'system:role:add', 3, 8, NULL, NULL, NULL, NULL, NULL),
('perm-role-edit-0000000000000001', '编辑角色', 'system:role:edit', 3, 8, NULL, NULL, NULL, NULL, NULL),
('perm-role-del-0000000000000001', '删除角色', 'system:role:delete', 3, 8, NULL, NULL, NULL, NULL, NULL),
('perm-role-list-0000000000000001', '查询角色', 'system:role:list', 3, 8, NULL, NULL, NULL, NULL, NULL),
('perm-role-perm-000000000000001', '分配权限', 'system:role:assign', 3, 8, NULL, NULL, NULL, NULL, NULL),

-- 6. 权限管理 - 菜单 (parent = 系统管理)
('perm-perm-menu-000000000000001', '权限管理', 'system:permission', 2, 1, '/system/permission', 'system/permission/PermissionList', 'IconMenu', 'PermissionList', 3),

-- 7. 权限管理 - 按钮权限
('perm-perm-add-0000000000000001', '新增权限', 'system:permission:add', 3, 14, NULL, NULL, NULL, NULL, NULL),
('perm-perm-edit-0000000000000001', '编辑权限', 'system:permission:edit', 3, 14, NULL, NULL, NULL, NULL, NULL),
('perm-perm-del-0000000000000001', '删除权限', 'system:permission:delete', 3, 14, NULL, NULL, NULL, NULL, NULL),
('perm-perm-list-0000000000000001', '查询权限', 'system:permission:list', 3, 14, NULL, NULL, NULL, NULL, NULL);

-- 为管理员角色分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT
    (SELECT id FROM sys_role WHERE name = 'ROLE_ADMIN'),
    id
FROM sys_permission;
