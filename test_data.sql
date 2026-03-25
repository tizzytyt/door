-- --------------------------------------------------------
-- 测试数据生成脚本
-- --------------------------------------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 用户数据 (密码均为 123456)
-- 学生
INSERT IGNORE INTO `user` (`username`, `password`, `real_name`, `role`, `phone`, `status`, `created_at`) VALUES
('student1', '123456', '张三', 'student', '13800138001', 1, NOW()),
('student2', '123456', '李四', 'student', '13800138002', 1, NOW()),
('student3', '123456', '王五', 'student', '13800138003', 1, NOW());

-- 管理员
INSERT IGNORE INTO `user` (`username`, `password`, `real_name`, `role`, `phone`, `status`, `created_at`) VALUES
('admin', '123456', '宿管阿姨', 'admin', '13900139001', 1, NOW());

-- 超级管理员
INSERT IGNORE INTO `user` (`username`, `password`, `real_name`, `role`, `phone`, `status`, `created_at`) VALUES
('super', '123456', '系统管理员', 'super_admin', '13900139002', 1, NOW());

-- 2. 门禁设备数据
INSERT IGNORE INTO `device` (`name`, `location`, `status`, `description`, `created_at`) VALUES
('东门门禁', 'A栋东侧大门', 1, '主出入口，全天开放', NOW()),
('西门门禁', 'A栋西侧侧门', 1, '仅限早晚高峰开放', NOW()),
('北门门禁', 'B栋北侧大门', 1, '靠近食堂', NOW()),
('南门门禁', 'B栋南侧后门', 2, '设备维护中，暂停使用', NOW());

-- 3. 系统配置数据 - 使用 ON DUPLICATE KEY UPDATE 避免重复
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES 
('max_reservations_per_day', '3', '每人每天最大预约次数'),
('reservation_lead_time_minutes', '60', '提前预约时间限制(分钟)'),
('auto_expire_minutes', '30', '预约开始后多久未使用自动失效(分钟)')
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `description` = VALUES(`description`);

-- 4. 预约记录数据 (模拟一些历史数据)
-- 先检查用户和设备是否存在，避免外键约束错误
INSERT IGNORE INTO `reservation` (`user_id`, `device_id`, `reservation_date`, `start_time`, `end_time`, `reason`, `status`, `created_at`) VALUES
(1, 1, CURDATE(), '18:00:00', '20:00:00', '晚自习回宿舍', 0, NOW());

INSERT IGNORE INTO `reservation` (`user_id`, `device_id`, `reservation_date`, `start_time`, `end_time`, `reason`, `status`, `created_at`) VALUES
(1, 3, CURDATE(), '12:00:00', '14:00:00', '午休回宿舍', 1, NOW());

INSERT IGNORE INTO `reservation` (`user_id`, `device_id`, `reservation_date`, `start_time`, `end_time`, `reason`, `status`, `audit_opinion`, `created_at`) VALUES
(2, 2, CURDATE(), '23:00:00', '23:59:00', '太晚了', 2, '超过门禁时间', NOW());

INSERT IGNORE INTO `reservation` (`user_id`, `device_id`, `reservation_date`, `start_time`, `end_time`, `reason`, `status`, `used_at`, `created_at`) VALUES
(2, 1, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '08:00:00', '10:00:00', '早起晨练', 3, NOW(), DATE_SUB(NOW(), INTERVAL 1 DAY));

-- --------------------------------------------------------
-- 追加：更多测试数据（可重复执行）
-- --------------------------------------------------------

-- 1) 追加更多学生用户（密码均为 123456）
INSERT IGNORE INTO `user` (`username`, `password`, `real_name`, `role`, `phone`, `status`, `created_at`) VALUES
('student4', '123456', '赵六', 'student', '13800138004', 1, NOW()),
('student5', '123456', '钱七', 'student', '13800138005', 1, NOW()),
('student6', '123456', '孙八', 'student', '13800138006', 1, NOW()),
('student7', '123456', '周九', 'student', '13800138007', 1, NOW()),
('student8', '123456', '吴十', 'student', '13800138008', 1, NOW()),
('student9', '123456', '郑十一', 'student', '13800138009', 1, NOW()),
('student10', '123456', '王十二', 'student', '13800138010', 1, NOW()),
('student11', '123456', '冯十三', 'student', '13800138011', 1, NOW()),
('student12', '123456', '陈十四', 'student', '13800138012', 1, NOW()),
('student13', '123456', '褚十五', 'student', '13800138013', 1, NOW()),
('student14', '123456', '卫十六', 'student', '13800138014', 1, NOW()),
('student15', '123456', '蒋十七', 'student', '13800138015', 1, NOW()),
('student16', '123456', '沈十八', 'student', '13800138016', 1, NOW()),
('student17', '123456', '韩十九', 'student', '13800138017', 1, NOW()),
('student18', '123456', '杨二十', 'student', '13800138018', 1, NOW()),
('student19', '123456', '朱二一', 'student', '13800138019', 1, NOW()),
('student20', '123456', '秦二二', 'student', '13800138020', 1, NOW());

-- 2) 追加更多门禁设备
INSERT IGNORE INTO `device` (`name`, `location`, `status`, `description`, `created_at`) VALUES
('A栋一层大门', 'A栋一层主入口', 1, '主出入口', NOW()),
('A栋侧门', 'A栋西侧侧门', 1, '侧门', NOW()),
('B栋一层大门', 'B栋一层主入口', 1, '主出入口', NOW()),
('B栋侧门', 'B栋南侧后门', 2, '维护中', NOW()),
('C栋一层大门', 'C栋一层主入口', 1, '主出入口', NOW()),
('C栋北门', 'C栋北侧小门', 0, '故障待修', NOW()),
('图书馆门禁', '图书馆一层入口', 1, '需预约进入', NOW()),
('实验楼门禁', '实验楼东门', 1, '夜间需预约', NOW());

-- 3) 生成预约（覆盖多日期/多状态）
SET @u_s1 := (SELECT id FROM `user` WHERE username='student1' LIMIT 1);
SET @u_s2 := (SELECT id FROM `user` WHERE username='student2' LIMIT 1);
SET @u_s3 := (SELECT id FROM `user` WHERE username='student3' LIMIT 1);
SET @u_s4 := (SELECT id FROM `user` WHERE username='student4' LIMIT 1);
SET @u_s5 := (SELECT id FROM `user` WHERE username='student5' LIMIT 1);
SET @u_s6 := (SELECT id FROM `user` WHERE username='student6' LIMIT 1);
SET @u_s7 := (SELECT id FROM `user` WHERE username='student7' LIMIT 1);
SET @u_s8 := (SELECT id FROM `user` WHERE username='student8' LIMIT 1);
SET @u_s9 := (SELECT id FROM `user` WHERE username='student9' LIMIT 1);
SET @u_s10 := (SELECT id FROM `user` WHERE username='student10' LIMIT 1);

SET @d_east := (SELECT id FROM `device` WHERE name='东门门禁' LIMIT 1);
SET @d_west := (SELECT id FROM `device` WHERE name='西门门禁' LIMIT 1);
SET @d_north := (SELECT id FROM `device` WHERE name='北门门禁' LIMIT 1);
SET @d_lib := (SELECT id FROM `device` WHERE name='图书馆门禁' LIMIT 1);
SET @d_lab := (SELECT id FROM `device` WHERE name='实验楼门禁' LIMIT 1);
SET @d_a1 := (SELECT id FROM `device` WHERE name='A栋一层大门' LIMIT 1);
SET @d_b1 := (SELECT id FROM `device` WHERE name='B栋一层大门' LIMIT 1);

-- 待审核（0）
INSERT INTO `reservation` (`user_id`,`device_id`,`reservation_date`,`start_time`,`end_time`,`reason`,`status`,`created_at`)
SELECT @u_s4, @d_a1, CURDATE(), '18:30:00','19:30:00','晚自习结束回宿舍',0, NOW()
WHERE @u_s4 IS NOT NULL AND @d_a1 IS NOT NULL;
INSERT INTO `reservation` (`user_id`,`device_id`,`reservation_date`,`start_time`,`end_time`,`reason`,`status`,`created_at`)
SELECT @u_s5, @d_b1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00','10:00:00','周末回寝室',0, NOW()
WHERE @u_s5 IS NOT NULL AND @d_b1 IS NOT NULL;

-- 已通过（1）
INSERT INTO `reservation` (`user_id`,`device_id`,`reservation_date`,`start_time`,`end_time`,`reason`,`status`,`audit_opinion`,`created_at`)
SELECT @u_s6, @d_east, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '20:00:00','21:00:00','参加社团活动回宿舍',1,'同意', DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE @u_s6 IS NOT NULL AND @d_east IS NOT NULL;
INSERT INTO `reservation` (`user_id`,`device_id`,`reservation_date`,`start_time`,`end_time`,`reason`,`status`,`audit_opinion`,`created_at`)
SELECT @u_s7, @d_lib, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '13:00:00','15:00:00','图书馆自习',1,'请按时进出', DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @u_s7 IS NOT NULL AND @d_lib IS NOT NULL;

-- 已拒绝（2）
INSERT INTO `reservation` (`user_id`,`device_id`,`reservation_date`,`start_time`,`end_time`,`reason`,`status`,`audit_opinion`,`created_at`)
SELECT @u_s8, @d_lab, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '23:30:00','23:59:00','实验未做完',2,'时间段不允许', DATE_SUB(NOW(), INTERVAL 3 DAY)
WHERE @u_s8 IS NOT NULL AND @d_lab IS NOT NULL;

-- 已使用（3）
INSERT INTO `reservation` (`user_id`,`device_id`,`reservation_date`,`start_time`,`end_time`,`reason`,`status`,`used_at`,`created_at`)
SELECT @u_s9, @d_west, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '07:30:00','08:30:00','晨跑回宿舍',3, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)
WHERE @u_s9 IS NOT NULL AND @d_west IS NOT NULL;

-- 已取消（4）
INSERT INTO `reservation` (`user_id`,`device_id`,`reservation_date`,`start_time`,`end_time`,`reason`,`status`,`created_at`)
SELECT @u_s10, @d_north, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '19:00:00','20:00:00','临时有事不回',4, DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @u_s10 IS NOT NULL AND @d_north IS NOT NULL;

-- 已失效（5）
INSERT INTO `reservation` (`user_id`,`device_id`,`reservation_date`,`start_time`,`end_time`,`reason`,`status`,`created_at`)
SELECT @u_s3, @d_east, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '18:00:00','19:00:00','错过时间',5, DATE_SUB(NOW(), INTERVAL 7 DAY)
WHERE @u_s3 IS NOT NULL AND @d_east IS NOT NULL;

-- 4) 访客预约数据
INSERT INTO `visitor_reservation` (`user_id`,`visitor_name`,`visitor_phone`,`visit_reason`,`visit_date`,`start_time`,`end_time`,`status`,`audit_opinion`,`created_at`)
SELECT @u_s1,'家长','13600000001','探望',CURDATE(),'18:00:00','19:00:00',1,'请携带身份证',NOW()
WHERE @u_s1 IS NOT NULL;
INSERT INTO `visitor_reservation` (`user_id`,`visitor_name`,`visitor_phone`,`visit_reason`,`visit_date`,`start_time`,`end_time`,`status`,`created_at`)
SELECT @u_s2,'同学','13600000002','送资料',DATE_ADD(CURDATE(), INTERVAL 1 DAY),'14:00:00','15:00:00',0,NOW()
WHERE @u_s2 IS NOT NULL;
INSERT INTO `visitor_reservation` (`user_id`,`visitor_name`,`visitor_phone`,`visit_reason`,`visit_date`,`start_time`,`end_time`,`status`,`audit_opinion`,`created_at`)
SELECT @u_s3,'快递员','13600000003','送快递',DATE_SUB(CURDATE(), INTERVAL 2 DAY),'10:00:00','10:30:00',2,'请走快递点流程',DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE @u_s3 IS NOT NULL;

-- 5) 报备数据（晚归/外出）
INSERT INTO `report` (`user_id`,`type`,`expected_time`,`reason`,`status`,`audit_opinion`,`created_at`)
SELECT @u_s4,1,CONCAT(CURDATE(),' 23:30:00'),'社团活动晚归',1,'注意安全',NOW()
WHERE @u_s4 IS NOT NULL;
INSERT INTO `report` (`user_id`,`type`,`expected_time`,`reason`,`status`,`created_at`)
SELECT @u_s5,2,CONCAT(DATE_ADD(CURDATE(), INTERVAL 1 DAY),' 09:00:00'),'临时外出办事',0,NOW()
WHERE @u_s5 IS NOT NULL;
INSERT INTO `report` (`user_id`,`type`,`expected_time`,`reason`,`status`,`audit_opinion`,`created_at`)
SELECT @u_s6,1,CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY),' 00:10:00'),'打工晚归',2,'请提前报备',DATE_SUB(NOW(), INTERVAL 2 DAY)
WHERE @u_s6 IS NOT NULL;

-- 6) 出入记录数据（部分异常）
INSERT INTO `access_record` (`user_id`,`device_id`,`access_time`,`type`,`reservation_id`,`status`)
SELECT @u_s1, @d_east, DATE_SUB(NOW(), INTERVAL 1 DAY), 1, NULL, 1
WHERE @u_s1 IS NOT NULL AND @d_east IS NOT NULL;
INSERT INTO `access_record` (`user_id`,`device_id`,`access_time`,`type`,`reservation_id`,`status`)
SELECT @u_s1, @d_east, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 30 MINUTE, 2, NULL, 1
WHERE @u_s1 IS NOT NULL AND @d_east IS NOT NULL;
INSERT INTO `access_record` (`user_id`,`device_id`,`access_time`,`type`,`reservation_id`,`status`)
SELECT @u_s2, @d_west, DATE_SUB(NOW(), INTERVAL 3 DAY), 1, NULL, 0
WHERE @u_s2 IS NOT NULL AND @d_west IS NOT NULL;

-- 7) 反馈数据（报修/建议）
INSERT INTO `feedback` (`user_id`,`type`,`content`,`images`,`status`,`admin_reply`,`created_at`)
SELECT @u_s7, 1, '西门门禁刷卡无反应，疑似读卡器故障', NULL, 1, '已安排维修人员检查', DATE_SUB(NOW(), INTERVAL 1 DAY)
WHERE @u_s7 IS NOT NULL;
INSERT INTO `feedback` (`user_id`,`type`,`content`,`images`,`status`,`admin_reply`,`created_at`)
SELECT @u_s8, 3, '建议增加节假日特殊预约规则说明', NULL, 2, '已采纳，后续更新公告', DATE_SUB(NOW(), INTERVAL 5 DAY)
WHERE @u_s8 IS NOT NULL;

-- 8) 收藏数据
INSERT IGNORE INTO `favorite` (`user_id`,`device_id`,`created_at`)
SELECT @u_s1, @d_east, NOW() WHERE @u_s1 IS NOT NULL AND @d_east IS NOT NULL;
INSERT IGNORE INTO `favorite` (`user_id`,`device_id`,`created_at`)
SELECT @u_s1, @d_lib, NOW() WHERE @u_s1 IS NOT NULL AND @d_lib IS NOT NULL;
INSERT IGNORE INTO `favorite` (`user_id`,`device_id`,`created_at`)
SELECT @u_s2, @d_west, NOW() WHERE @u_s2 IS NOT NULL AND @d_west IS NOT NULL;

-- 9) 黑名单数据（演示：封禁用户）
SET @u_s20 := (SELECT id FROM `user` WHERE username='student20' LIMIT 1);
INSERT INTO `blacklist` (`user_id`,`reason`,`expiry_date`,`created_at`)
SELECT @u_s20, '多次违规使用门禁', DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()
WHERE @u_s20 IS NOT NULL;
UPDATE `user` SET status = 0 WHERE id = @u_s20;

SET FOREIGN_KEY_CHECKS = 1;