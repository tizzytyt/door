-- 执行前提：已存在 reservation、notification 表（见 access_control.sql / db_design.sql）。
-- 预约开始前提醒（仅发送一次）：与 notification 表 type=1 配合使用。
ALTER TABLE `reservation`
  ADD COLUMN `reminder_sent` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已发送开始前提醒：0-否,1-是' AFTER `audit_opinion`;
