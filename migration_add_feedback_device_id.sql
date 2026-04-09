-- 已有库升级：为 feedback 增加门禁关联（报修必选门禁）
-- 在 MySQL 中执行一次即可

-- 幂等升级：重复执行不会报 “Duplicate column / Duplicate FK”

-- 1) 若 feedback.device_id 不存在，则添加
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'feedback'
        AND COLUMN_NAME = 'device_id'
    ),
    'SELECT ''feedback.device_id already exists'';',
    'ALTER TABLE `feedback` ADD COLUMN `device_id` BIGINT NULL COMMENT ''关联门禁（报修必填；建议等可为空）'' AFTER `user_id`;'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 若还没有从 feedback.device_id -> device.id 的外键，则添加
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.KEY_COLUMN_USAGE
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'feedback'
        AND COLUMN_NAME = 'device_id'
        AND REFERENCED_TABLE_NAME = 'device'
        AND REFERENCED_COLUMN_NAME = 'id'
    ),
    'SELECT ''feedback.device_id FK already exists'';',
    'ALTER TABLE `feedback` ADD CONSTRAINT `fk_feedback_device` FOREIGN KEY (`device_id`) REFERENCES `device`(`id`);'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
