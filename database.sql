CREATE DATABASE IF NOT EXISTS scoring_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE scoring_system;
-- Spring Boot 启动后会自动创建业务表。以下为初始管理员账号：admin / admin123
-- 密码字段为 BCrypt 哈希，应用首次启动时由 DataInitializer 写入。
