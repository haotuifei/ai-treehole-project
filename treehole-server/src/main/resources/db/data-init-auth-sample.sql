-- =============================================================================
-- 认证模块示例数据（MySQL 8.0）
-- 用途：手工初始化或核对与 DataInitializer 一致的演示账号
-- 说明：
--   1. 请先执行 schema.sql 建表
--   2. 应用首次启动时 DataInitializer 会自动创建相同逻辑的数据（防重复）
--   3. 密码均为 BCrypt(10)，明文见注释
-- =============================================================================

SET NAMES utf8mb4;

-- 预置角色（不存在则插入）
INSERT INTO sys_role (role_code, role_name, remark)
SELECT 'ADMIN', '管理员', '预置' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = 'ADMIN');

INSERT INTO sys_role (role_code, role_name, remark)
SELECT 'COUNSELOR', '辅导员', '预置' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = 'COUNSELOR');

INSERT INTO sys_role (role_code, role_name, remark)
SELECT 'STUDENT', '学生', '预置' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = 'STUDENT');

-- 管理员 admin / admin123
INSERT INTO sys_user (username, password, real_name, status, deleted)
SELECT 'admin', '$2a$10$WXjxEBA8TuXsSwF3zN9aSuHtHOQxbGNpILbPy8zHn1lrQMMQ/5a9G', '系统管理员', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin' AND deleted = 0);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- 辅导员 counselor / counselor123
INSERT INTO sys_user (username, password, real_name, status, deleted)
SELECT 'counselor', '$2a$10$Rs7zaB9DBBl4CLo7T/wez.Insd9/1Emvsv.OL3W1P2BMREP3PknOm', '演示辅导员', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'counselor' AND deleted = 0);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'counselor' AND r.role_code = 'COUNSELOR'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);

-- 学生 student / student123
INSERT INTO sys_user (username, password, real_name, status, deleted)
SELECT 'student', '$2a$10$6idKYF41nwMvRMNRHzsMeuB82I8gNMzyvpHSRW/eC/EDj2aKWQBQC', '演示学生', 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'student' AND deleted = 0);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id FROM sys_user u, sys_role r
WHERE u.username = 'student' AND r.role_code = 'STUDENT'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
