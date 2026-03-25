-- --------------------------------------------------------
-- 基于小程序的门禁预约管理平台 - 数据库设计脚本
-- --------------------------------------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 用户表 (user)
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名/学号/工号',
  `password` VARCHAR(255) NOT NULL COMMENT '密码',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `role` ENUM('student', 'admin', 'super_admin') NOT NULL DEFAULT 'student' COMMENT '角色：student-学生, admin-管理员, super_admin-系统管理员',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar` VARCHAR(255) COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 0-禁用/黑名单',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 门禁设备表 (device)
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `name` VARCHAR(100) NOT NULL COMMENT '门禁名称 (如: A栋宿舍门)',
  `location` VARCHAR(255) COMMENT '具体位置',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 2-维护中, 0-故障',
  `description` TEXT COMMENT '设备描述',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备表';

-- 3. 预约申请表 (reservation)
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `reservation_date` DATE NOT NULL COMMENT '预约日期',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `reason` VARCHAR(255) COMMENT '预约事由',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核, 1-已通过, 2-已拒绝, 3-已使用, 4-已取消, 5-已失效',
  `audit_opinion` VARCHAR(255) COMMENT '审核意见',
  `used_at` DATETIME COMMENT '实际使用时间',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`device_id`) REFERENCES `device`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约申请表';

-- 4. 访客预约表 (visitor_reservation)
DROP TABLE IF EXISTS `visitor_reservation`;
CREATE TABLE `visitor_reservation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '访客预约ID',
  `user_id` BIGINT NOT NULL COMMENT '申请学生ID',
  `visitor_name` VARCHAR(50) NOT NULL COMMENT '访客姓名',
  `visitor_phone` VARCHAR(20) NOT NULL COMMENT '访客电话',
  `visit_reason` VARCHAR(255) COMMENT '来访事由',
  `visit_date` DATE NOT NULL COMMENT '来访日期',
  `start_time` TIME NOT NULL COMMENT '预计到达时间',
  `end_time` TIME NOT NULL COMMENT '预计离开时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核, 1-已通过, 2-已拒绝, 3-已取消',
  `audit_opinion` VARCHAR(255) COMMENT '审核意见',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访客预约表';

-- 5. 报备表 (report) - 晚归/临时外出
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '报备ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '类型：1-晚归, 2-临时外出',
  `expected_time` DATETIME NOT NULL COMMENT '预计时间',
  `reason` VARCHAR(255) COMMENT '原因说明',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待审核, 1-已通过, 2-已拒绝, 3-已撤销',
  `audit_opinion` VARCHAR(255) COMMENT '审核意见',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报备表';

-- 6. 出入记录表 (access_record)
DROP TABLE IF EXISTS `access_record`;
CREATE TABLE `access_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `access_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出入时间',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '出入类型：1-进入, 2-离开',
  `reservation_id` BIGINT COMMENT '关联预约ID (如果有)',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 0-异常',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`device_id`) REFERENCES `device`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出入记录表';

-- 7. 报修与反馈表 (feedback)
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL COMMENT '反馈类型：1-报修, 2-投诉, 3-建议',
  `content` TEXT NOT NULL COMMENT '反馈内容',
  `images` TEXT COMMENT '图片URL列表 (JSON或逗号分隔)',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理, 1-处理中, 2-已完成',
  `admin_reply` TEXT COMMENT '管理员回复',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报修与反馈表';

-- 8. 门禁收藏表 (favorite)
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `device_id` BIGINT NOT NULL COMMENT '设备ID',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_device` (`user_id`, `device_id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`),
  FOREIGN KEY (`device_id`) REFERENCES `device`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁收藏表';

-- 9. 黑名单记录表 (blacklist)
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `reason` VARCHAR(255) COMMENT '拉黑原因',
  `expiry_date` DATETIME COMMENT '到期时间 (为空表示永久)',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='黑名单记录表';

-- 10. 系统配置表 (system_config)
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '配置项Key',
  `config_value` VARCHAR(255) NOT NULL COMMENT '配置项值',
  `description` VARCHAR(255) COMMENT '描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入一些初始配置数据
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES 
('max_reservations_per_day', '3', '每人每天最大预约次数'),
('reservation_lead_time_minutes', '60', '提前预约时间限制(分钟)'),
('auto_expire_minutes', '30', '预约开始后多久未使用自动失效(分钟)');

-- 11. 消息通知表 (notification)
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
  `type` TINYINT COMMENT '通知类型：1-预约提醒, 2-审核结果, 3-系统公告',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  FOREIGN KEY (`user_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

SET FOREIGN_KEY_CHECKS = 1;
