-- 为 feedback 增加对话消息表
-- 建议：先在本地数据库执行一次即可

-- 幂等升级：表存在则跳过创建；若缺外键则补齐

CREATE TABLE IF NOT EXISTS `feedback_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `feedback_id` BIGINT NOT NULL COMMENT '反馈ID',
  `sender_user_id` BIGINT NOT NULL COMMENT '发送用户ID',
  `sender_role` VARCHAR(20) NOT NULL COMMENT '发送者角色：student/admin/super_admin',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`feedback_id`) REFERENCES `feedback`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`sender_user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修对话消息表';

-- 若表已存在但缺少外键（常见于先建表后补约束），则补齐
-- 1) feedback_message.feedback_id -> feedback.id (并保证级联删除)
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.KEY_COLUMN_USAGE
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'feedback_message'
        AND COLUMN_NAME = 'feedback_id'
        AND REFERENCED_TABLE_NAME = 'feedback'
        AND REFERENCED_COLUMN_NAME = 'id'
    ),
    'SELECT ''feedback_message.feedback_id FK already exists'';',
    'ALTER TABLE `feedback_message` ADD CONSTRAINT `fk_feedback_message_feedback` FOREIGN KEY (`feedback_id`) REFERENCES `feedback`(`id`) ON DELETE CASCADE;'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) feedback_message.sender_user_id -> user.id
SET @sql := (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.KEY_COLUMN_USAGE
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'feedback_message'
        AND COLUMN_NAME = 'sender_user_id'
        AND REFERENCED_TABLE_NAME = 'user'
        AND REFERENCED_COLUMN_NAME = 'id'
    ),
    'SELECT ''feedback_message.sender_user_id FK already exists'';',
    'ALTER TABLE `feedback_message` ADD CONSTRAINT `fk_feedback_message_sender_user` FOREIGN KEY (`sender_user_id`) REFERENCES `user`(`id`);'
  )
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

