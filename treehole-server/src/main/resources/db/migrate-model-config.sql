-- 迁移脚本：将 application.yml 中的 AI 配置插入到 model_config 表
-- 执行方式：在 MySQL 中运行此脚本

USE treehole;

-- 插入当前的 MiniMax 配置
INSERT INTO model_config (provider, api_base, model_name, api_key_cipher, enabled, priority, remark)
VALUES (
    'minimax',
    'https://api.minimaxi.com/anthropic',
    'MiniMax M2.5',
    'sk-cp-Dkm5lvehvQyOB6j-GJP8YcyuUJe9dI9NOECtsn9JskchJishOWbTm96nzeUuHIJLrIqDs3CATT3HEjBj3Mk7Kxq-u4hbt6wLZBg8gGA_MDacU-LQ2g2kHwU',
    1,
    10,
    'MiniMax M2.5 模型配置（从 application.yml 迁移）'
);
