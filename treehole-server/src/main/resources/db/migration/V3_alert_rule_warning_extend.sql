-- 已有库升级：预警规则表 + warning_record 扩展字段（按需执行）
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS alert_rule (
    id                              BIGINT          NOT NULL AUTO_INCREMENT,
    rule_code                       VARCHAR(32)     NOT NULL DEFAULT 'DEFAULT',
    medium_sentiment_threshold      DECIMAL(6,3)    NOT NULL DEFAULT -0.250,
    high_sentiment_threshold        DECIMAL(6,3)    NOT NULL DEFAULT -0.550,
    keyword_high_enabled            TINYINT         NOT NULL DEFAULT 1,
    remark                          VARCHAR(255)    DEFAULT NULL,
    deleted                         TINYINT         NOT NULL DEFAULT 0,
    create_time                     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time                     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_alert_rule_code (rule_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO alert_rule (rule_code, medium_sentiment_threshold, high_sentiment_threshold, keyword_high_enabled, remark)
SELECT 'DEFAULT', -0.250, -0.550, 1, '系统默认'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM alert_rule WHERE rule_code = 'DEFAULT' AND deleted = 0);

-- warning_record 扩展（列已存在则跳过）
-- ALTER TABLE warning_record ADD COLUMN session_id BIGINT DEFAULT NULL COMMENT '关联AI会话' AFTER user_id;
-- ALTER TABLE warning_record ADD COLUMN text_summary VARCHAR(512) DEFAULT NULL AFTER emotion_record_id;
-- ALTER TABLE warning_record ADD COLUMN trigger_source VARCHAR(16) DEFAULT NULL AFTER text_summary;
-- ALTER TABLE warning_record ADD KEY idx_warning_session (session_id);
