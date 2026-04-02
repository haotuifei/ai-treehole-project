-- =============================================================================
-- 备考目标 + 学习打卡：从旧版 schema 升级的参考脚本（请按库内实际情况分步执行）
-- 新环境请直接使用 db/schema.sql 中的最新建表语句。
-- =============================================================================
-- study_goal 变更要点：
--   title → goal_name；新增 goal_type、start_date、remark；target_date → end_date
--   旧 status=2（放弃）建议 UPDATE 为 3；新语义：0进行中 1已完成 2已暂停 3已放弃
-- study_checkin 变更要点：
--   新增 goal_id、pomodoro_done、mood、remark；可删除 mood_score（若存在）
--   同一用户+目标+日期 的唯一性由应用层校验（逻辑删除下不宜用简单 UNIQUE）
-- =============================================================================

SET NAMES utf8mb4;

-- 示例（若列已存在请跳过对应语句）：
-- ALTER TABLE study_goal CHANGE COLUMN title goal_name VARCHAR(128) NOT NULL COMMENT '目标名称';
-- ALTER TABLE study_goal ADD COLUMN goal_type VARCHAR(32) NOT NULL DEFAULT 'CUSTOM' COMMENT '目标类型' AFTER goal_name;
-- ALTER TABLE study_goal ADD COLUMN start_date DATE DEFAULT NULL AFTER goal_type;
-- ALTER TABLE study_goal CHANGE COLUMN target_date end_date DATE DEFAULT NULL COMMENT '截止日期';
-- ALTER TABLE study_goal ADD COLUMN remark VARCHAR(512) DEFAULT NULL COMMENT '备注' AFTER description;
-- UPDATE study_goal SET status = 3 WHERE status = 2;

-- ALTER TABLE study_checkin ADD COLUMN goal_id BIGINT NULL COMMENT '所属目标' AFTER user_id;
-- （为历史行补全 goal_id 后）ALTER TABLE study_checkin MODIFY goal_id BIGINT NOT NULL;
-- ALTER TABLE study_checkin ADD COLUMN pomodoro_done TINYINT NOT NULL DEFAULT 0 COMMENT '是否完成番茄钟' AFTER duration_minutes;
-- ALTER TABLE study_checkin ADD COLUMN mood VARCHAR(64) DEFAULT NULL COMMENT '今日心情' AFTER pomodoro_done;
-- ALTER TABLE study_checkin ADD COLUMN remark VARCHAR(512) DEFAULT NULL COMMENT '备注' AFTER mood;
-- ALTER TABLE study_checkin ADD KEY idx_checkin_goal (goal_id);
-- ALTER TABLE study_checkin ADD KEY idx_checkin_user_goal_date (user_id, goal_id, check_date);
