-- ============================================================
-- 粉色衣橱 (PinkCloset) 数据库初始化脚本
-- 自动创建数据库与核心表结构
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `pink_closet_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `pink_closet_db`;

-- ============================================================
-- 用户表 (user)
-- ============================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name`        VARCHAR(50)  NOT NULL                COMMENT '姓名',
  `gender`      VARCHAR(10)  DEFAULT NULL            COMMENT '性别：男/女/其他',
  `age`         INT          DEFAULT NULL            COMMENT '年龄',
  `phone`       VARCHAR(20)  NOT NULL                COMMENT '手机号（唯一，可用于登录）',
  `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱（可用于登录）',
  `password`    VARCHAR(255) NOT NULL                COMMENT '密码（明文存储）',
  `avatar`      VARCHAR(500) DEFAULT NULL            COMMENT '头像路径',
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '0=正常 1=已注销（软删除）',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='用户信息表';

-- ============================================================
-- 超级管理员表 (user_root) — 与普通用户分开存储
-- ============================================================
DROP TABLE IF EXISTS `user_root`;
CREATE TABLE `user_root` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username`    VARCHAR(50)  NOT NULL                COMMENT '管理员用户名',
  `password`    VARCHAR(255) NOT NULL                COMMENT '密码（明文存储）',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='超级管理员表';

-- 插入默认超级管理员 root / Swh2000@..
INSERT INTO `user_root` (`username`, `password`) VALUES ('root', 'Swh2000@..');

-- ============================================================
-- 搭配方案表 (outfit_plan) — 先删，因为有外键依赖
-- ============================================================
DROP TABLE IF EXISTS `outfit_plan`;

-- ============================================================
-- 衣物表 (clothing_item) — 按 user_id 隔离数据
-- ============================================================
DROP TABLE IF EXISTS `clothing_item`;
CREATE TABLE `clothing_item` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '所属用户ID（0=公共/旧数据）',
  `name`        VARCHAR(100) NOT NULL                COMMENT '衣物名称',
  `category`    VARCHAR(20)  NOT NULL                COMMENT '分类: TOP(上衣) / BOTTOM(裤子) / SHOES(鞋子) / ACCESSORY(配饰)',
  `image_url`   VARCHAR(500) DEFAULT NULL            COMMENT '图片存储路径',
  `color`       VARCHAR(50)  DEFAULT NULL            COMMENT '主色调',
  `style`       VARCHAR(100) DEFAULT NULL            COMMENT '风格标签（逗号分隔，如: 甜美, 休闲）',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='衣物信息表';

-- ============================================================
-- 搭配方案表 (outfit_plan) — 按 user_id 隔离数据
-- ============================================================
CREATE TABLE `outfit_plan` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '搭配方案ID',
  `user_id`       BIGINT       NOT NULL DEFAULT 0      COMMENT '所属用户ID（0=公共/旧数据）',
  `name`          VARCHAR(100) DEFAULT '我的搭配'    COMMENT '搭配名称',
  `top_id`        BIGINT       DEFAULT NULL           COMMENT '上衣ID (关联 clothing_item.id)',
  `bottom_id`     BIGINT       DEFAULT NULL           COMMENT '裤子ID (关联 clothing_item.id)',
  `shoes_id`      BIGINT       DEFAULT NULL           COMMENT '鞋子ID (关联 clothing_item.id)',
  `accessory_id`  BIGINT       DEFAULT NULL           COMMENT '配饰ID (关联 clothing_item.id)',
  `note`          VARCHAR(500) DEFAULT NULL           COMMENT '备注',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  CONSTRAINT `fk_top`       FOREIGN KEY (`top_id`)       REFERENCES `clothing_item`(`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_bottom`    FOREIGN KEY (`bottom_id`)    REFERENCES `clothing_item`(`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_shoes`     FOREIGN KEY (`shoes_id`)     REFERENCES `clothing_item`(`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_accessory` FOREIGN KEY (`accessory_id`) REFERENCES `clothing_item`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='搭配方案表';

-- ============================================================
-- 示例数据（user_id=0 表示公共示例数据）
-- ============================================================
INSERT INTO `clothing_item` (`user_id`, `name`, `category`, `color`, `style`) VALUES
(0, '白色棉质T恤',  'TOP',       '白色', '基础款, 百搭'),
(0, '粉色碎花衬衫', 'TOP',       '粉色', '甜美, 少女'),
(0, '米色针织毛衣', 'TOP',       '米色', '温柔, 秋冬'),
(0, '浅蓝牛仔裤',  'BOTTOM',    '蓝色', '休闲, 百搭'),
(0, '白色A字裙',   'BOTTOM',    '白色', '甜美, 少女'),
(0, '黑色打底裤',  'BOTTOM',    '黑色', '百搭, 显瘦'),
(0, '白色小白鞋',  'SHOES',     '白色', '休闲, 百搭'),
(0, '粉色帆布鞋',  'SHOES',     '粉色', '甜美, 少女'),
(0, '米色厚底鞋',  'SHOES',     '米色', '显高, 气质'),
(0, '珍珠项链',    'ACCESSORY', '银色', '优雅, 甜美'),
(0, '草莓发卡',    'ACCESSORY', '红色', '可爱, 少女');

-- 重置自增起点为 1
ALTER TABLE `clothing_item` AUTO_INCREMENT = 1;

-- ============================================================
-- 管理员常用 SQL（仅供参考，不自动执行）
-- ============================================================
-- 恢复已注销用户：
-- UPDATE `user` SET `deleted` = 0 WHERE `id` = ?;
--
-- 彻底删除用户及关联数据（不可恢复）：
-- DELETE FROM `outfit_plan`   WHERE `user_id` = ?;
-- DELETE FROM `clothing_item` WHERE `user_id` = ?;
-- DELETE FROM `user`          WHERE `id` = ?;
