-- ============================================================
-- Phase 1: 初始化数据库建表 DDL
-- 说明: 所有对外暴露的标识使用 UUID 32位无横线格式
--       内部关联使用自增主键，保证查询性能
-- ============================================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部自增主键，仅用于关联查询',
    uuid VARCHAR(32) NOT NULL COMMENT '对外暴露的唯一标识（UUID 32位无横线）',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=启用 0=禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_uuid (uuid),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部自增主键',
    uuid VARCHAR(32) NOT NULL COMMENT '对外UUID',
    name VARCHAR(50) NOT NULL COMMENT '角色名称（如 ROLE_ADMIN, ROLE_USER）',
    description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=启用 0=禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_uuid (uuid),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部自增主键',
    user_id BIGINT NOT NULL COMMENT '关联 sys_user.id',
    role_id BIGINT NOT NULL COMMENT '关联 sys_role.id',
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表';

-- 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部自增主键',
    uuid VARCHAR(32) NOT NULL COMMENT '对外UUID',
    name VARCHAR(100) NOT NULL COMMENT '权限名称（显示在菜单/树中）',
    code VARCHAR(100) NOT NULL COMMENT '权限编码（如 system:user:list）',
    type TINYINT NOT NULL COMMENT '类型: 1=目录 2=菜单 3=按钮 4=API',
    parent_id BIGINT DEFAULT NULL COMMENT '父级ID（关联本表id，构建树形结构）',
    path VARCHAR(255) DEFAULT NULL COMMENT '前端路由路径（如 /system/user）',
    component VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径（如 system/user/UserList）',
    icon VARCHAR(100) DEFAULT NULL COMMENT '菜单图标名（如 IconUser）',
    route_name VARCHAR(100) DEFAULT NULL COMMENT '路由名称（如 UserList）',
    keep_alive TINYINT DEFAULT 1 COMMENT '是否缓存页面: 1=是 0=否',
    hidden TINYINT DEFAULT 0 COMMENT '是否隐藏（不在菜单显示）: 1=是 0=否',
    method VARCHAR(10) DEFAULT NULL COMMENT 'API请求方式: GET/POST/PUT/DELETE',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=启用 0=禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_uuid (uuid),
    UNIQUE KEY uk_code (code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表（资源/按钮/前端页面级别）';

-- 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部自增主键',
    role_id BIGINT NOT NULL COMMENT '关联 sys_role.id',
    permission_id BIGINT NOT NULL COMMENT '关联 sys_permission.id',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES sys_permission(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部自增主键',
    user_id BIGINT DEFAULT NULL COMMENT '用户ID（关联 sys_user.id）',
    username VARCHAR(50) DEFAULT NULL COMMENT '登录用户名',
    login_type TINYINT DEFAULT NULL COMMENT '登录方式: 1=密码 2=短信验证码',
    ip VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
    status TINYINT DEFAULT NULL COMMENT '状态: 1=成功 0=失败',
    fail_reason VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 技能组表
CREATE TABLE IF NOT EXISTS skill_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部自增主键',
    uuid VARCHAR(32) NOT NULL COMMENT '对外UUID',
    name VARCHAR(100) NOT NULL COMMENT '技能组名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    folder_names VARCHAR(2000) NOT NULL DEFAULT '["references","scripts","templates"]' COMMENT '子文件夹名JSON数组',
    owner_id BIGINT DEFAULT NULL COMMENT '负责人ID，关联sys_user.id',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=启用 0=删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_sg_uuid (uuid),
    UNIQUE KEY uk_sg_name (name),
    KEY idx_sg_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能组表';

-- 技能文件表
CREATE TABLE IF NOT EXISTS skill_file (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '内部自增主键',
    uuid VARCHAR(32) NOT NULL COMMENT '对外UUID',
    group_id BIGINT NOT NULL COMMENT '关联skill_group.id',
    folder_name VARCHAR(100) NOT NULL COMMENT '所属子文件夹名',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    oss_key VARCHAR(500) NOT NULL COMMENT 'OSS对象Key',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    file_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=启用 0=删除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_sf_uuid (uuid),
    KEY idx_sf_group_id (group_id),
    CONSTRAINT fk_sf_group FOREIGN KEY (group_id) REFERENCES skill_group(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能文件表';
