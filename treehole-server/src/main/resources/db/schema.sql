-- AI 树洞系统 - MySQL 8.0 核心表结构
-- 使用前请先: CREATE DATABASE treehole DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户与 RBAC
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    username        VARCHAR(64)     NOT NULL COMMENT '登录名',
    password        VARCHAR(128)    NOT NULL COMMENT 'BCrypt 密文',
    real_name       VARCHAR(64)     DEFAULT NULL COMMENT '真实姓名',
    phone           VARCHAR(20)     DEFAULT NULL COMMENT '手机号',
    email           VARCHAR(128)    DEFAULT NULL COMMENT '邮箱',
    student_no      VARCHAR(32)     DEFAULT NULL COMMENT '学号',
    class_name      VARCHAR(64)     DEFAULT NULL COMMENT '班级名称',
    counselor_id    BIGINT          DEFAULT NULL COMMENT '辅导员用户ID',
    avatar_url      VARCHAR(512)    DEFAULT NULL COMMENT '头像URL',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username),
    KEY idx_sys_user_student_no (student_no),
    KEY idx_sys_user_counselor_id (counselor_id),
    KEY idx_sys_user_status_deleted (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

CREATE TABLE sys_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    role_code       VARCHAR(32)     NOT NULL COMMENT '角色编码 ADMIN/COUNSELOR/STUDENT',
    role_name       VARCHAR(64)     NOT NULL COMMENT '角色名称',
    remark          VARCHAR(255)    DEFAULT NULL COMMENT '备注',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE sys_user_role (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_sys_user_role_user_id (user_id),
    KEY idx_sys_user_role_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ----------------------------
-- 学习相关
-- ----------------------------
DROP TABLE IF EXISTS study_checkin;
DROP TABLE IF EXISTS study_goal;

CREATE TABLE study_goal (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT          NOT NULL COMMENT '学生用户ID',
    goal_name       VARCHAR(128)    NOT NULL COMMENT '目标名称',
    goal_type       VARCHAR(32)     NOT NULL DEFAULT 'CUSTOM' COMMENT 'POSTGRAD考研/CIVIL_SERVICE考公/COURSE课程/CUSTOM自定义',
    start_date      DATE            DEFAULT NULL COMMENT '开始日期',
    end_date        DATE            DEFAULT NULL COMMENT '截止日期',
    description     TEXT            COMMENT '总目标描述',
    status          TINYINT         NOT NULL DEFAULT 0 COMMENT '0进行中1已完成2已暂停3已放弃',
    remark          VARCHAR(512)    DEFAULT NULL COMMENT '备注',
    sort_order      INT             NOT NULL DEFAULT 0 COMMENT '排序',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_study_goal_user_id (user_id),
    KEY idx_study_goal_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='备考目标表';

CREATE TABLE study_checkin (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id             BIGINT          NOT NULL COMMENT '学生用户ID',
    goal_id             BIGINT          NOT NULL COMMENT '所属备考目标ID',
    check_date          DATE            NOT NULL COMMENT '打卡日期',
    content             VARCHAR(1024)   DEFAULT NULL COMMENT '打卡内容',
    duration_minutes    INT             DEFAULT NULL COMMENT '学习时长(分钟)',
    pomodoro_done       TINYINT         NOT NULL DEFAULT 0 COMMENT '是否完成番茄钟 0否1是',
    mood                VARCHAR(64)     DEFAULT NULL COMMENT '今日心情',
    remark              VARCHAR(512)    DEFAULT NULL COMMENT '备注',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_checkin_user_date (user_id, check_date),
    KEY idx_checkin_goal (goal_id),
    KEY idx_checkin_user_goal_date (user_id, goal_id, check_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习打卡表';

-- ----------------------------
-- 辅导员-班级关联
-- ----------------------------
DROP TABLE IF EXISTS counselor_class;

CREATE TABLE counselor_class (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    counselor_user_id   BIGINT          NOT NULL COMMENT '辅导员用户ID',
    class_name          VARCHAR(64)     NOT NULL COMMENT '班级名称',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除 0否 1是',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_counselor_class (counselor_user_id, class_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='辅导员-班级关联表';

-- ----------------------------
-- AI 对话
-- ----------------------------
DROP TABLE IF EXISTS ai_chat_message;
DROP TABLE IF EXISTS ai_chat_session;

CREATE TABLE ai_chat_session (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT          NOT NULL COMMENT '学生用户ID',
    title           VARCHAR(255)    DEFAULT NULL COMMENT '会话标题',
    last_message_at DATETIME        DEFAULT NULL COMMENT '最后消息时间',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_ai_session_user (user_id),
    KEY idx_ai_session_user_last (user_id, last_message_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话会话表';

CREATE TABLE ai_chat_message (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    session_id      BIGINT          NOT NULL COMMENT '会话ID',
    message_role    VARCHAR(16)     NOT NULL COMMENT 'user/assistant/system',
    content         MEDIUMTEXT      NOT NULL COMMENT '消息内容',
    token_estimate  INT             DEFAULT NULL COMMENT '估算token数',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_ai_msg_session_time (session_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息表';

-- ----------------------------
-- 情绪与预警
-- ----------------------------
DROP TABLE IF EXISTS intervention_record;
DROP TABLE IF EXISTS warning_record;
DROP TABLE IF EXISTS emotion_record;
DROP TABLE IF EXISTS alert_rule;

CREATE TABLE alert_rule (
    id                              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    rule_code                       VARCHAR(32)     NOT NULL DEFAULT 'DEFAULT' COMMENT '规则编码',
    medium_sentiment_threshold      DECIMAL(6,3)    NOT NULL DEFAULT -0.250 COMMENT '情绪分值<=此视为中风险(区间[-1,1]越低越消极)',
    high_sentiment_threshold        DECIMAL(6,3)    NOT NULL DEFAULT -0.550 COMMENT '情绪分值<=此视为高风险(情绪维)',
    keyword_high_enabled            TINYINT         NOT NULL DEFAULT 1 COMMENT '是否启用关键词直判高风险',
    remark                          VARCHAR(255)    DEFAULT NULL,
    deleted                         TINYINT         NOT NULL DEFAULT 0,
    create_time                     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time                     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_alert_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警阈值规则(可运行版单条DEFAULT)';

CREATE TABLE emotion_record (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT          NOT NULL COMMENT '学生用户ID',
    session_id      BIGINT          DEFAULT NULL COMMENT '关联会话',
    message_id      BIGINT          DEFAULT NULL COMMENT '关联消息',
    sentiment_score DECIMAL(6,3)    DEFAULT NULL COMMENT '情绪分值',
    emotion_label   VARCHAR(32)     DEFAULT NULL COMMENT '情绪标签',
    risk_level      VARCHAR(16)     NOT NULL DEFAULT 'LOW' COMMENT 'LOW/MEDIUM/HIGH',
    analysis_detail TEXT            COMMENT '分析明细JSON',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_emotion_user_time (user_id, create_time),
    KEY idx_emotion_risk (risk_level, create_time),
    KEY idx_emotion_session (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情绪分析记录表';

CREATE TABLE warning_record (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id             BIGINT          NOT NULL COMMENT '学生用户ID',
    session_id          BIGINT          DEFAULT NULL COMMENT '关联AI会话',
    emotion_record_id   BIGINT          DEFAULT NULL COMMENT '关联情绪记录',
    text_summary        VARCHAR(512)    DEFAULT NULL COMMENT '触发文本摘要',
    trigger_source      VARCHAR(16)     DEFAULT NULL COMMENT 'KEYWORD/EMOTION/BOTH',
    risk_level          VARCHAR(16)     NOT NULL COMMENT '风险等级',
    status              VARCHAR(16)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/RESOLVED/CLOSED',
    handler_user_id     BIGINT          DEFAULT NULL COMMENT '处理人(辅导员/管理员)',
    handle_remark       VARCHAR(512)    DEFAULT NULL COMMENT '处理备注',
    handled_at          DATETIME        DEFAULT NULL COMMENT '处理完成时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_warning_user (user_id),
    KEY idx_warning_session (session_id),
    KEY idx_warning_status_time (status, create_time),
    KEY idx_warning_emotion (emotion_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预警记录表';

CREATE TABLE intervention_record (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    warning_record_id   BIGINT          DEFAULT NULL COMMENT '关联预警',
    student_user_id     BIGINT          NOT NULL COMMENT '学生用户ID',
    counselor_user_id   BIGINT          NOT NULL COMMENT '辅导员用户ID',
    content             TEXT            NOT NULL COMMENT '干预内容',
    intervention_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '干预时间',
    deleted             TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_intervention_student (student_user_id),
    KEY idx_intervention_counselor (counselor_user_id),
    KEY idx_intervention_warning (warning_record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='干预记录表';

-- ----------------------------
-- 系统与模型配置
-- ----------------------------
DROP TABLE IF EXISTS system_log;
DROP TABLE IF EXISTS model_config;

CREATE TABLE system_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id         BIGINT          DEFAULT NULL COMMENT '操作人',
    module          VARCHAR(64)     DEFAULT NULL COMMENT '模块',
    operation       VARCHAR(128)    DEFAULT NULL COMMENT '操作描述',
    method          VARCHAR(256)    DEFAULT NULL COMMENT '方法签名',
    request_uri     VARCHAR(512)    DEFAULT NULL COMMENT '请求URI',
    request_method  VARCHAR(16)     DEFAULT NULL COMMENT 'HTTP方法',
    ip              VARCHAR(64)     DEFAULT NULL COMMENT 'IP',
    user_agent      VARCHAR(512)    DEFAULT NULL COMMENT 'UA',
    status          TINYINT         NOT NULL DEFAULT 1 COMMENT '0失败1成功',
    error_msg       VARCHAR(1024)   DEFAULT NULL COMMENT '错误信息',
    duration_ms     INT             DEFAULT NULL COMMENT '耗时毫秒',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_sys_log_user_time (user_id, create_time),
    KEY idx_sys_log_module_time (module, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统操作日志表';

CREATE TABLE model_config (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    provider        VARCHAR(32)     NOT NULL COMMENT '厂商 deepseek/qwen/kimi',
    api_base        VARCHAR(255)    NOT NULL COMMENT 'API Base URL',
    model_name      VARCHAR(128)    NOT NULL COMMENT '模型名',
    api_key_cipher  VARCHAR(512)    DEFAULT NULL COMMENT 'API Key密文',
    enabled         TINYINT         NOT NULL DEFAULT 1 COMMENT '是否启用',
    priority        INT             NOT NULL DEFAULT 0 COMMENT '优先级，越大优先',
    remark          VARCHAR(255)    DEFAULT NULL COMMENT '备注',
    deleted         TINYINT         NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_model_enabled_priority (enabled, priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='大模型配置表';

SET FOREIGN_KEY_CHECKS = 1;
