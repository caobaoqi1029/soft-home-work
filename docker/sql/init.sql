/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : localhost:3306
 Source Schema         : monitor

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 09/04/2024 17:07:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_account
-- ----------------------------
DROP TABLE IF EXISTS `tb_account`;
CREATE TABLE `tb_account`
(
    `id`            int                                                           NOT NULL AUTO_INCREMENT COMMENT 'id',
    `username`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
    `email`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
    `password`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
    `role`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色',
    `clients`       json                                                          NULL COMMENT '所拥有主机',
    `register_time` datetime                                                      NULL DEFAULT NULL COMMENT '注册时间',
    `avatar`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `unique_email` (`email` ASC) USING BTREE,
    UNIQUE INDEX `unique_username` (`username` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_account
-- ----------------------------
INSERT INTO `tb_account`
VALUES (1, 'cbq', '2024cbq@gmail.com', '$2a$10$JWdC4ukpwGh4sSmjIjKyYeTAPiasgKOp73Wa6k4TFy8asyqIIftS2', 'admin', NULL,
        '2024-03-30 17:21:56', 'https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/avatar.png');

-- ----------------------------
-- Table structure for tb_client
-- ----------------------------
DROP TABLE IF EXISTS `tb_client`;
CREATE TABLE `tb_client`
(
    `id`            int                                                           NOT NULL COMMENT 'id',
    `name`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
    `token`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'token',
    `location`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '区域',
    `node`          varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点',
    `register_time` datetime                                                      NULL DEFAULT NULL COMMENT '注册时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tb_client_detail
-- ----------------------------
DROP TABLE IF EXISTS `tb_client_detail`;
CREATE TABLE `tb_client_detail`
(
    `id`         int                                                           NOT NULL COMMENT 'id',
    `os_arch`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统架构',
    `os_name`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统名称',
    `os_version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '系统版本',
    `os_bit`     int                                                           NULL DEFAULT NULL COMMENT '系统位数',
    `cpu_name`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'CPU 名称',
    `cpu_core`   int                                                           NULL DEFAULT NULL COMMENT 'CPU 核心数',
    `memory`     double                                                        NULL DEFAULT NULL COMMENT '内存',
    `disk`       double                                                        NULL DEFAULT NULL COMMENT '磁盘',
    `ip`         varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'ip',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for tb_client_ssh
-- ----------------------------
DROP TABLE IF EXISTS `tb_client_ssh`;
CREATE TABLE `tb_client_ssh`
(
    `id`       int                                                           NOT NULL COMMENT 'id',
    `port`     int                                                           NULL DEFAULT NULL COMMENT '端口',
    `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
    `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of tb_client_ssh
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;

