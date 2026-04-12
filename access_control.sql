/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : access_control

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 12/04/2026 18:15:04
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for access_record
-- ----------------------------
DROP TABLE IF EXISTS `access_record`;
CREATE TABLE `access_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `access_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '出入时间',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '出入类型：1-进入, 2-离开',
  `reservation_id` bigint NULL DEFAULT NULL COMMENT '关联预约ID (如果有)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 0-异常',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `device_id`(`device_id` ASC) USING BTREE,
  CONSTRAINT `access_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `access_record_ibfk_2` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '出入记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of access_record
-- ----------------------------
INSERT INTO `access_record` VALUES (1, 1, 1, '2026-04-11 18:05:43', 1, NULL, 1);
INSERT INTO `access_record` VALUES (2, 1, 1, '2026-04-11 18:35:43', 2, NULL, 1);
INSERT INTO `access_record` VALUES (3, 2, 2, '2026-04-09 18:05:43', 1, NULL, 0);

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公告内容',
  `publisher_id` bigint NOT NULL COMMENT '发布人ID(管理员)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `publisher_id`(`publisher_id` ASC) USING BTREE,
  CONSTRAINT `announcement_ibfk_1` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------

-- ----------------------------
-- Table structure for announcement_read
-- ----------------------------
DROP TABLE IF EXISTS `announcement_read`;
CREATE TABLE `announcement_read`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回执ID',
  `announcement_id` bigint NOT NULL COMMENT '公告ID',
  `user_id` bigint NOT NULL COMMENT '学生用户ID',
  `read_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '已读时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_announcement_user`(`announcement_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `announcement_read_ibfk_1` FOREIGN KEY (`announcement_id`) REFERENCES `announcement` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `announcement_read_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公告已读回执表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement_read
-- ----------------------------

-- ----------------------------
-- Table structure for blacklist
-- ----------------------------
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '黑名单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '拉黑原因',
  `expiry_date` datetime NULL DEFAULT NULL COMMENT '到期时间 (为空表示永久)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `blacklist_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '黑名单记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of blacklist
-- ----------------------------
INSERT INTO `blacklist` VALUES (1, 22, '多次违规使用门禁', '2026-04-19 18:05:43', '2026-04-12 18:05:43');

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门禁名称 (如: A栋宿舍门)',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '具体位置',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 2-维护中, 0-故障',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '设备描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '门禁设备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device
-- ----------------------------
INSERT INTO `device` VALUES (1, '东门门禁', 'A栋东侧大门', 1, '主出入口，全天开放', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (2, '西门门禁', 'A栋西侧侧门', 1, '仅限早晚高峰开放', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (3, '北门门禁', 'B栋北侧大门', 1, '靠近食堂', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (4, '南门门禁', 'B栋南侧后门', 2, '设备维护中，暂停使用', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (5, 'A栋一层大门', 'A栋一层主入口', 1, '主出入口', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (6, 'A栋侧门', 'A栋西侧侧门', 1, '侧门', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (7, 'B栋一层大门', 'B栋一层主入口', 1, '主出入口', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (8, 'B栋侧门', 'B栋南侧后门', 2, '维护中', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (9, 'C栋一层大门', 'C栋一层主入口', 1, '主出入口', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (10, 'C栋北门', 'C栋北侧小门', 0, '故障待修', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (11, '图书馆门禁', '图书馆一层入口', 1, '需预约进入', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `device` VALUES (12, '实验楼门禁', '实验楼东门', 1, '夜间需预约', '2026-04-12 18:05:43', '2026-04-12 18:05:43');

-- ----------------------------
-- Table structure for favorite
-- ----------------------------
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_device`(`user_id` ASC, `device_id` ASC) USING BTREE,
  INDEX `device_id`(`device_id` ASC) USING BTREE,
  CONSTRAINT `favorite_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `favorite_ibfk_2` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '门禁收藏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite
-- ----------------------------
INSERT INTO `favorite` VALUES (1, 1, 1, '2026-04-12 18:05:43');
INSERT INTO `favorite` VALUES (2, 1, 11, '2026-04-12 18:05:43');
INSERT INTO `favorite` VALUES (3, 2, 2, '2026-04-12 18:05:43');

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_id` bigint NULL DEFAULT NULL COMMENT '关联门禁（报修必填；建议等可为空）',
  `type` tinyint NOT NULL COMMENT '反馈类型：1-报修, 2-投诉, 3-建议',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '反馈内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片URL列表 (JSON或逗号分隔)',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理, 1-处理中, 2-已完成',
  `admin_reply` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '管理员回复',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `device_id`(`device_id` ASC) USING BTREE,
  CONSTRAINT `feedback_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `feedback_ibfk_2` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修与反馈表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of feedback
-- ----------------------------
INSERT INTO `feedback` VALUES (1, 9, 2, 1, '西门门禁刷卡无反应，疑似读卡器故障', NULL, 1, '已安排维修人员检查', '2026-04-11 18:05:43');
INSERT INTO `feedback` VALUES (2, 10, NULL, 3, '建议增加节假日特殊预约规则说明', NULL, 2, '已采纳，后续更新公告', '2026-04-07 18:05:43');

-- ----------------------------
-- Table structure for feedback_message
-- ----------------------------
DROP TABLE IF EXISTS `feedback_message`;
CREATE TABLE `feedback_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `feedback_id` bigint NOT NULL COMMENT '反馈ID',
  `sender_user_id` bigint NOT NULL COMMENT '发送用户ID',
  `sender_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送者角色：student/admin/super_admin',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `feedback_id`(`feedback_id` ASC) USING BTREE,
  INDEX `sender_user_id`(`sender_user_id` ASC) USING BTREE,
  CONSTRAINT `feedback_message_ibfk_1` FOREIGN KEY (`feedback_id`) REFERENCES `feedback` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `feedback_message_ibfk_2` FOREIGN KEY (`sender_user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报修对话消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of feedback_message
-- ----------------------------

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
  `type` tinyint NULL DEFAULT NULL COMMENT '通知类型：1-预约提醒, 2-审核结果, 3-系统公告, 4-管理员报修通知',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notification
-- ----------------------------

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报备ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `type` tinyint NOT NULL COMMENT '类型：1-晚归, 2-临时外出',
  `expected_time` datetime NOT NULL COMMENT '预计时间',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原因说明',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待审核, 1-已通过, 2-已拒绝, 3-已撤销',
  `audit_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `report_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报备表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report
-- ----------------------------
INSERT INTO `report` VALUES (1, 6, 1, '2026-04-12 23:30:00', '社团活动晚归', 1, '注意安全', '2026-04-12 18:05:43');
INSERT INTO `report` VALUES (2, 7, 2, '2026-04-13 09:00:00', '临时外出办事', 0, NULL, '2026-04-12 18:05:43');
INSERT INTO `report` VALUES (3, 8, 1, '2026-04-10 00:10:00', '打工晚归', 2, '请提前报备', '2026-04-10 18:05:43');

-- ----------------------------
-- Table structure for reservation
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `reservation_date` date NOT NULL COMMENT '预约日期',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预约事由',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待审核, 1-已通过, 2-已拒绝, 3-已使用, 4-已取消, 5-已失效',
  `audit_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `reminder_sent` tinyint NOT NULL DEFAULT 0 COMMENT '是否已发送开始前提醒：0-否,1-是',
  `used_at` datetime NULL DEFAULT NULL COMMENT '实际使用时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `device_id`(`device_id` ASC) USING BTREE,
  CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reservation
-- ----------------------------
INSERT INTO `reservation` VALUES (1, 1, 1, '2026-04-12', '18:00:00', '20:00:00', '晚自习回宿舍', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (2, 1, 3, '2026-04-12', '12:00:00', '14:00:00', '午休回宿舍', 1, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (3, 2, 2, '2026-04-12', '23:00:00', '23:59:00', '太晚了', 2, '超过门禁时间', 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (4, 2, 1, '2026-04-11', '08:00:00', '10:00:00', '早起晨练', 3, NULL, 0, '2026-04-12 18:05:43', '2026-04-11 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (5, 6, 5, '2026-04-12', '18:30:00', '19:30:00', '晚自习结束回宿舍', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (6, 7, 7, '2026-04-13', '09:00:00', '10:00:00', '周末回寝室', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (7, 8, 1, '2026-04-10', '20:00:00', '21:00:00', '参加社团活动回宿舍', 1, '同意', 0, NULL, '2026-04-10 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (8, 9, 11, '2026-04-11', '13:00:00', '15:00:00', '图书馆自习', 1, '请按时进出', 0, NULL, '2026-04-11 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (9, 10, 12, '2026-04-09', '23:30:00', '23:59:00', '实验未做完', 2, '时间段不允许', 0, NULL, '2026-04-09 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (10, 11, 2, '2026-04-08', '07:30:00', '08:30:00', '晨跑回宿舍', 3, NULL, 0, '2026-04-08 18:05:43', '2026-04-08 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (11, 12, 3, '2026-04-11', '19:00:00', '20:00:00', '临时有事不回', 4, NULL, 0, NULL, '2026-04-11 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (12, 3, 1, '2026-04-05', '18:00:00', '19:00:00', '错过时间', 5, NULL, 0, NULL, '2026-04-05 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (13, 1, 1, '2026-04-02', '07:30:00', '08:00:00', '晨跑回寝', 3, NULL, 0, '2026-04-02 18:05:43', '2026-04-02 18:05:43', '2026-04-02 18:05:43');
INSERT INTO `reservation` VALUES (14, 1, 2, '2026-04-03', '22:00:00', '22:30:00', '自习结束返回', 5, NULL, 0, NULL, '2026-04-03 18:05:43', '2026-04-03 18:05:43');
INSERT INTO `reservation` VALUES (15, 1, 3, '2026-04-04', '18:30:00', '19:00:00', '社团活动后回宿舍', 1, '同意', 0, NULL, '2026-04-04 18:05:43', '2026-04-04 18:05:43');
INSERT INTO `reservation` VALUES (16, 1, 11, '2026-04-05', '20:00:00', '20:30:00', '图书馆闭馆后返回', 2, '时间段不合规', 0, NULL, '2026-04-05 18:05:43', '2026-04-05 18:05:43');
INSERT INTO `reservation` VALUES (17, 1, 12, '2026-04-06', '09:00:00', '09:30:00', '实验课提前入楼', 3, NULL, 0, '2026-04-06 18:05:43', '2026-04-06 18:05:43', '2026-04-06 18:05:43');
INSERT INTO `reservation` VALUES (18, 1, 1, '2026-04-07', '12:00:00', '12:30:00', '午休回寝', 4, NULL, 0, NULL, '2026-04-07 18:05:43', '2026-04-07 18:05:43');
INSERT INTO `reservation` VALUES (19, 1, 2, '2026-04-08', '19:00:00', '19:30:00', '晚饭后回宿舍', 1, '同意', 0, NULL, '2026-04-08 18:05:43', '2026-04-08 18:05:43');
INSERT INTO `reservation` VALUES (20, 1, 3, '2026-04-09', '21:00:00', '21:30:00', '自习结束', 3, NULL, 0, '2026-04-09 18:05:43', '2026-04-09 18:05:43', '2026-04-09 18:05:43');
INSERT INTO `reservation` VALUES (21, 1, 11, '2026-04-10', '17:00:00', '17:30:00', '借书返回', 1, '同意', 0, NULL, '2026-04-10 18:05:43', '2026-04-10 18:05:43');
INSERT INTO `reservation` VALUES (22, 1, 12, '2026-04-11', '23:00:00', '23:30:00', '晚实验结束', 2, '超出允许时段', 0, NULL, '2026-04-11 18:05:43', '2026-04-11 18:05:43');
INSERT INTO `reservation` VALUES (23, 1, 1, '2026-04-12', '06:00:00', '06:30:00', '晨练返校', 3, NULL, 0, '2026-04-12 18:05:43', '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (24, 1, 2, '2026-04-12', '20:30:00', '21:00:00', '晚自习后回寝', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (25, 1, 3, '2026-04-13', '07:30:00', '08:00:00', '早课前回宿舍拿资料', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (26, 1, 11, '2026-04-14', '18:00:00', '18:30:00', '晚间去图书馆', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (27, 1, 12, '2026-04-15', '09:30:00', '10:00:00', '实验楼课程', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (28, 1, 1, '2026-04-16', '13:00:00', '13:30:00', '午后返校', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (29, 1, 2, '2026-04-17', '19:30:00', '20:00:00', '社团活动结束', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `reservation` VALUES (30, 1, 3, '2026-04-18', '21:30:00', '22:00:00', '夜间自习后返回', 0, NULL, 0, NULL, '2026-04-12 18:05:43', '2026-04-12 18:05:43');

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项Key',
  `config_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'max_reservations_per_day', '3', '每人每天最大预约次数');
INSERT INTO `system_config` VALUES (2, 'reservation_lead_time_minutes', '60', '提前预约时间限制(分钟)');
INSERT INTO `system_config` VALUES (3, 'auto_expire_minutes', '30', '预约开始后多久未使用自动失效(分钟)');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名/学号/工号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `role` enum('student','admin','super_admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'student' COMMENT '角色：student-学生, admin-管理员, super_admin-系统管理员',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-正常, 0-禁用/黑名单',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'student1', '123456', '张三', 'student', '13800138001', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (2, 'student2', '123456', '李四', 'student', '13800138002', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (3, 'student3', '123456', '王五', 'student', '13800138003', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (4, 'admin', '123456', '宿管阿姨', 'admin', '13900139001', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (5, 'super', '123456', '系统管理员', 'super_admin', '13900139002', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (6, 'student4', '123456', '赵六', 'student', '13800138004', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (7, 'student5', '123456', '钱七', 'student', '13800138005', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (8, 'student6', '123456', '孙八', 'student', '13800138006', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (9, 'student7', '123456', '周九', 'student', '13800138007', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (10, 'student8', '123456', '吴十', 'student', '13800138008', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (11, 'student9', '123456', '郑十一', 'student', '13800138009', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (12, 'student10', '123456', '王十二', 'student', '13800138010', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (13, 'student11', '123456', '冯十三', 'student', '13800138011', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (14, 'student12', '123456', '陈十四', 'student', '13800138012', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (15, 'student13', '123456', '褚十五', 'student', '13800138013', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (16, 'student14', '123456', '卫十六', 'student', '13800138014', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (17, 'student15', '123456', '蒋十七', 'student', '13800138015', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (18, 'student16', '123456', '沈十八', 'student', '13800138016', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (19, 'student17', '123456', '韩十九', 'student', '13800138017', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (20, 'student18', '123456', '杨二十', 'student', '13800138018', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (21, 'student19', '123456', '朱二一', 'student', '13800138019', NULL, 1, '2026-04-12 18:05:43', '2026-04-12 18:05:43');
INSERT INTO `user` VALUES (22, 'student20', '123456', '秦二二', 'student', '13800138020', NULL, 0, '2026-04-12 18:05:43', '2026-04-12 18:05:43');

-- ----------------------------
-- Table structure for visitor_reservation
-- ----------------------------
DROP TABLE IF EXISTS `visitor_reservation`;
CREATE TABLE `visitor_reservation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '访客预约ID',
  `user_id` bigint NOT NULL COMMENT '申请学生ID',
  `visitor_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访客姓名',
  `visitor_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '访客电话',
  `visit_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '来访事由',
  `visit_date` date NOT NULL COMMENT '来访日期',
  `start_time` time NOT NULL COMMENT '预计到达时间',
  `end_time` time NOT NULL COMMENT '预计离开时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-待审核, 1-已通过, 2-已拒绝, 3-已取消',
  `audit_opinion` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `visitor_reservation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '访客预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visitor_reservation
-- ----------------------------
INSERT INTO `visitor_reservation` VALUES (1, 1, '家长', '13600000001', '探望', '2026-04-12', '18:00:00', '19:00:00', 1, '请携带身份证', '2026-04-12 18:05:43');
INSERT INTO `visitor_reservation` VALUES (2, 2, '同学', '13600000002', '送资料', '2026-04-13', '14:00:00', '15:00:00', 0, NULL, '2026-04-12 18:05:43');
INSERT INTO `visitor_reservation` VALUES (3, 3, '快递员', '13600000003', '送快递', '2026-04-10', '10:00:00', '10:30:00', 2, '请走快递点流程', '2026-04-10 18:05:43');

SET FOREIGN_KEY_CHECKS = 1;
