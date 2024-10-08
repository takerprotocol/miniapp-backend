/*
 Navicat Premium Dump SQL

 Source Server         : docker-mysql
 Source Server Type    : MySQL
 Source Server Version : 50738 (5.7.38)
 Source Host           : localhost:3306
 Source Schema         : demo

 Target Server Type    : MySQL
 Target Server Version : 50738 (5.7.38)
 File Encoding         : 65001

 Date: 04/09/2024 19:29:28
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for bool_assignment
-- ----------------------------
DROP TABLE IF EXISTS `bool_assignment`;
CREATE TABLE `bool_assignment` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '修改人',
  `f_name` varchar(100) DEFAULT '' COMMENT '任务名称',
  `f_title` varchar(255) NOT NULL DEFAULT '' COMMENT '任务标题',
  `f_describe` text COMMENT '描述',
  `f_url` text COMMENT '跳转链接',
  `f_online` int(10) unsigned NOT NULL DEFAULT '1' COMMENT '是否在线 0不在线 1在线',
  `f_flag` varchar(100) NOT NULL DEFAULT '' COMMENT '携带标志',
  `f_type` int(11) NOT NULL DEFAULT '0' COMMENT '任务类别 1 关注twitter 2加入社群',
  `reward_value` decimal(38,18) NOT NULL DEFAULT '0.000000000000000000' COMMENT '奖励值',
  `sort_field` int(11) NOT NULL DEFAULT '999999999' COMMENT '排序字段',
  `f_project_item` varchar(100) NOT NULL DEFAULT '' COMMENT '发行方，bool,partner',
  `f_project_logo` varchar(100) DEFAULT NULL COMMENT '项目方logo',
  `top` tinyint(1) DEFAULT NULL COMMENT '置顶',
  `top_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '置顶时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=84 DEFAULT CHARSET=utf8mb4 COMMENT='小任务表';

-- ----------------------------
-- Records of bool_assignment
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_assignment_user_relation
-- ----------------------------
DROP TABLE IF EXISTS `bool_assignment_user_relation`;
CREATE TABLE `bool_assignment_user_relation` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `f_assignment_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '任务ID',
  `f_user_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '修改人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_assignment` (`f_user_id`,`f_assignment_id`),
  KEY `normal_assignment` (`f_assignment_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=339 DEFAULT CHARSET=utf8mb4 COMMENT='任务用户关联关系表';

-- ----------------------------
-- Records of bool_assignment_user_relation
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_calculate_rank_offset
-- ----------------------------
DROP TABLE IF EXISTS `bool_calculate_rank_offset`;
CREATE TABLE `bool_calculate_rank_offset` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `f_latest_reward_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '最新奖励id',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '修改人',
  `f_type` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '类别 1积分排行计算逻辑  2全量邀请排行计算逻辑',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='计算排名任务偏移量';

-- ----------------------------
-- Records of bool_calculate_rank_offset
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_stake_apply_record
-- ----------------------------
DROP TABLE IF EXISTS `bool_stake_apply_record`;
CREATE TABLE `bool_stake_apply_record` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '修改人',
  `user_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '用户id ',
  `amounts` text COMMENT '数量，多个以,分隔',
  `deviceids` text COMMENT '设备id,多个以,分隔',
  `f_address` varchar(100) NOT NULL DEFAULT '' COMMENT '用户地址',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`) USING BTREE,
  KEY `idx_address` (`f_address`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8mb4 COMMENT='质押申请记录，只要来这里拼过交易都会记录一笔，无论最终是否提交上链';

-- ----------------------------
-- Records of bool_stake_apply_record
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_sync_config
-- ----------------------------
DROP TABLE IF EXISTS `bool_sync_config`;
CREATE TABLE `bool_sync_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `private_key` varchar(255) DEFAULT NULL COMMENT '私钥',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8mb4 COMMENT='多地址私钥存储表，打币用';

-- ----------------------------
-- Records of bool_sync_config
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_user_custom_info
-- ----------------------------
DROP TABLE IF EXISTS `bool_user_custom_info`;
CREATE TABLE `bool_user_custom_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '修改人',
  `f_type` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '1 自定义url  还有其它未补充',
  `f_value` varchar(2000) NOT NULL DEFAULT '' COMMENT '自定义值',
  `f_user_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '用户id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_type` (`f_user_id`,`f_type`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='用户自定义信息';

-- ----------------------------
-- Records of bool_user_custom_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_user_invitation_relation
-- ----------------------------
DROP TABLE IF EXISTS `bool_user_invitation_relation`;
CREATE TABLE `bool_user_invitation_relation` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  `inviter_id` bigint(20) DEFAULT NULL COMMENT '邀请人ID',
  `invitee_id` bigint(20) DEFAULT NULL COMMENT '被邀请人ID',
  `invitation_time` datetime DEFAULT NULL COMMENT '邀请时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_invitee` (`invitee_id`) USING BTREE,
  KEY `idx_search` (`inviter_id`,`invitation_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户邀请关系表';

-- ----------------------------
-- Records of bool_user_invitation_relation
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_user_invite_count_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `bool_user_invite_count_snapshot`;
CREATE TABLE `bool_user_invite_count_snapshot` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '修改人',
  `f_user_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
  `f_amount` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '邀请人数量1级+2级',
  `f_amount_1` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '一级邀请人数',
  `f_amount_2` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '二级邀请人数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user` (`f_user_id`),
  KEY `idx_sort` (`f_amount`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=425 DEFAULT CHARSET=utf8mb4 COMMENT='用户邀请数量全量快照表';

-- ----------------------------
-- Records of bool_user_invite_count_snapshot
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_user_invite_count_week_snapshot
-- ----------------------------
DROP TABLE IF EXISTS `bool_user_invite_count_week_snapshot`;
CREATE TABLE `bool_user_invite_count_week_snapshot` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `create_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
    `update_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '修改人',
    `f_user_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
    `f_amount` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '邀请人数量1级+2级',
    `f_caculate_timestamp` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '业务时间',
    `f_amount_1` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '一级邀请人数',
    `f_amount_2` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '二级邀请人数',
    `f_latest_invitation_timestamp` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '最晚达标时间记录',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uniq_time_user` (`f_caculate_timestamp`,`f_user_id`),
    KEY `idx_sort_1` (`f_caculate_timestamp`,`f_amount`,`f_latest_invitation_timestamp`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=85809350 DEFAULT CHARSET=utf8mb4 COMMENT='用户邀请数量周维度快照表';

-- ----------------------------
-- Records of bool_user_invite_count_week_snapshot
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_user_invite_count_week_settlement_flow
-- ----------------------------
DROP TABLE IF EXISTS `bool_user_invite_count_week_settlement_flow`;
CREATE TABLE `bool_user_invite_count_week_settlement_flow` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建人',
  `update_user` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '修改人',
  `f_user_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '用户ID',
  `f_amount` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '邀请人数量1级+2级',
  `f_calculate_timestamp` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '业务时间',
  `f_snapshot_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '对应快照记录ID',
  `f_award` varchar(100) DEFAULT '0' COMMENT '奖励',
  `f_rank` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '排名 1-10',
  `f_draw_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '对应抽奖记录ID，周结算奖也放入抽奖记录中，方便提币',
  `f_status` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '发放状态  0未发放  1已发放，此时会有drawid，2已领取，提币触发',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_settle` (`f_calculate_timestamp`,`f_user_id`),
  UNIQUE KEY `uniq_snap` (`f_snapshot_id`),
  KEY `idx_draw` (`f_draw_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COMMENT='周榜结算流水';

-- ----------------------------
-- Records of bool_user_invite_count_week_settlement_flow
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_user_private_key_fragment_info
-- ----------------------------
DROP TABLE IF EXISTS `bool_user_private_key_fragment_info`;
CREATE TABLE `bool_user_private_key_fragment_info` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `key_type` varchar(255) DEFAULT NULL COMMENT '私钥碎片类型',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  `public_key_address` varchar(200) DEFAULT NULL COMMENT '公钥地址',
  `private_key1fragment` text COMMENT '私钥 1 碎片',
  `private_key2fragment` text COMMENT '私钥 2 碎片',
  `private_key3fragment` text COMMENT '私钥 3 碎片',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_key` (`user_id`,`key_type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Mpc 钱包私钥碎片表';

-- ----------------------------
-- Records of bool_user_private_key_fragment_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_user_reward_record
-- ----------------------------
DROP TABLE IF EXISTS `bool_user_reward_record`;
CREATE TABLE `bool_user_reward_record` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  `reward_type` varchar(255) NOT NULL DEFAULT 'ES' COMMENT '奖励类型类型',
  `reward_value` decimal(38,18) DEFAULT NULL COMMENT '奖励值',
  `user_id` varchar(100) DEFAULT NULL COMMENT '用户id ',
  `reward_time` datetime DEFAULT NULL COMMENT '结算时间',
  `addition_info` mediumtext CHARACTER SET utf8mb4 COMMENT '奖励补充信息\n',
  `tx_hash` varchar(255) DEFAULT NULL COMMENT '奖励交易hash\n',
  `tx_status` varchar(255) NOT NULL DEFAULT 'PENDING' COMMENT '奖励交易状态\n',
  `tx_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `type_user` (`reward_type`,`user_id`) USING BTREE,
  KEY `status_tx` (`tx_status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户奖励记录表\n';

-- ----------------------------
-- Records of bool_user_reward_record
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for bool_user_user
-- ----------------------------
DROP TABLE IF EXISTS `bool_user_user`;
CREATE TABLE `bool_user_user` (
  `id` bigint(20) NOT NULL COMMENT 'ID',
  `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `create_user` bigint(20) DEFAULT NULL COMMENT '创建人',
  `update_user` bigint(20) DEFAULT NULL COMMENT '修改人',
  `user_tg_id` bigint(20) NOT NULL COMMENT 'TG ID',
  `username` mediumtext COMMENT '用户名\n',
  `first_name` varchar(100) DEFAULT NULL COMMENT 'TG First Name',
  `last_name` varchar(100) DEFAULT NULL COMMENT 'TG Last Name',
  `invitation_code` varchar(255) CHARACTER SET utf8 DEFAULT NULL COMMENT '用户唯一邀请码\n',
  `introduction` mediumtext COMMENT '介绍\n',
  `additional_info` text CHARACTER SET utf8 COMMENT '额外信息',
  `address` varchar(255) DEFAULT NULL COMMENT 'EVM地址',
  `reward_amount` decimal(38,18) NOT NULL DEFAULT '0.000000000000000000' COMMENT '获得的总奖励数量',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `unique_tg_user` (`user_tg_id`) USING BTREE,
  KEY `idx_rank` (`reward_amount`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- Records of bool_user_user
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
