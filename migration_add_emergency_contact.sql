-- 学生紧急联系人表（已有库执行）
CREATE TABLE IF NOT EXISTS `emergency_contact` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '联系人ID',
  `user_id` BIGINT NOT NULL COMMENT '学生用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `relation` VARCHAR(20) NULL DEFAULT NULL COMMENT '关系：父母/配偶/亲友/其他',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  CONSTRAINT `fk_emergency_contact_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生紧急联系人表';
