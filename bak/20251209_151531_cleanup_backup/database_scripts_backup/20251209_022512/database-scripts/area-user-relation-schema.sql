-- =====================================================
-- IOE-DREAM 区域-人员关联表结构
-- 完善区域作为空间概念的完整业务逻辑
-- 支持人员信息下发到区域设备
-- =====================================================

-- 区域人员关联表
CREATE TABLE IF NOT EXISTS `t_area_user_relation` (
    `relation_id` VARCHAR(64) NOT NULL COMMENT '关联ID',
    `area_id` BIGINT NOT NULL COMMENT '区域ID',
    `area_code` VARCHAR(50) NOT NULL COMMENT '区域编码',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(100) NOT NULL COMMENT '用户名',
    `real_name` VARCHAR(100) NOT NULL COMMENT '真实姓名',
    `relation_type` TINYINT NOT NULL DEFAULT 1 COMMENT '关联类型：1-常驻人员 2-临时人员 3-访客 4-维护人员 5-管理人员',
    `permission_level` TINYINT NOT NULL DEFAULT 1 COMMENT '权限级别：1-只读 2-读写 3-管理 4-超级管理员',
    `relation_status` TINYINT NOT NULL DEFAULT 1 COMMENT '关联状态：1-正常 2-禁用 3-过期 4-待审批',
    `effective_time` DATETIME NULL DEFAULT NULL COMMENT '生效时间',
    `expire_time` DATETIME NULL DEFAULT NULL COMMENT '失效时间',
    `permanent` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否永久有效：0-否 1-是',
    `inherit_children` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否继承子区域权限：0-否 1-是',
    `inherit_parent` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否继承父区域权限：0-否 1-是',
    `allow_start_time` VARCHAR(10) NULL DEFAULT NULL COMMENT '允许进入开始时间：HH:mm',
    `allow_end_time` VARCHAR(10) NULL DEFAULT NULL COMMENT '允许进入结束时间：HH:mm',
    `workday_only` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否仅工作日：0-否 1-是',
    `access_permissions` JSON NULL DEFAULT NULL COMMENT '访问权限配置(JSON格式)',
    `device_sync_status` TINYINT NOT NULL DEFAULT 0 COMMENT '设备下发状态：0-未下发 1-下发中 2-下发成功 3-下发失败 4-已撤销',
    `last_sync_time` DATETIME NULL DEFAULT NULL COMMENT '最后下发时间',
    `sync_failure_reason` VARCHAR(500) NULL DEFAULT NULL COMMENT '下发失败原因',
    `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
    `extended_attributes` JSON NULL DEFAULT NULL COMMENT '扩展属性(JSON格式)',
    `remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_user_id` BIGINT NULL DEFAULT NULL COMMENT '创建人ID',
    `update_user_id` BIGINT NULL DEFAULT NULL COMMENT '更新人ID',
    `deleted_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`relation_id`),
    UNIQUE KEY `uk_area_user` (`area_id`, `user_id`, `deleted_flag`),
    KEY `idx_area_id` (`area_id`, `relation_status`, `deleted_flag`),
    KEY `idx_user_id` (`user_id`, `relation_status`, `deleted_flag`),
    KEY `idx_relation_type` (`relation_type`, `relation_status`, `deleted_flag`),
    KEY `idx_permission_level` (`permission_level`, `relation_status`, `deleted_flag`),
    KEY `idx_sync_status` (`device_sync_status`, `relation_status`, `deleted_flag`),
    KEY `idx_effective_time` (`effective_time`, `expire_time`, `relation_status`, `deleted_flag`),
    KEY `idx_create_time` (`create_time`, `deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='区域人员关联表';

-- 区域设备关联表（更新版本）
ALTER TABLE `t_area_device_relation`
ADD COLUMN IF NOT EXISTS `access_level` TINYINT NOT NULL DEFAULT 1 COMMENT '访问权限级别：1-只读 2-读写 3-管理 4-超级管理员' AFTER `priority`,
ADD COLUMN IF NOT EXISTS `user_sync_status` TINYINT NOT NULL DEFAULT 0 COMMENT '用户同步状态：0-未同步 1-同步中 2-同步成功 3-同步失败' AFTER `access_level`,
ADD COLUMN IF NOT EXISTS `last_user_sync_time` DATETIME NULL DEFAULT NULL COMMENT '最后用户同步时间' AFTER `user_sync_status`,
ADD INDEX IF NOT EXISTS `idx_access_level` (`access_level`, `relation_status`),
ADD INDEX IF NOT EXISTS `idx_user_sync_status` (`user_sync_status`, `relation_status`);

-- 区域表（增强版本）
ALTER TABLE `t_common_area`
ADD COLUMN IF NOT EXISTS `access_level` TINYINT NOT NULL DEFAULT 1 COMMENT '默认访问权限级别：1-公开 2-受限 3-私密 4-机密' AFTER `area_status`,
ADD COLUMN IF NOT EXISTS `need_card_access` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要刷卡进入：0-否 1-是' AFTER `access_level`,
ADD COLUMN IF NOT EXISTS `need_face_recognition` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要人脸识别：0-否 1-是' AFTER `need_card_access`,
ADD COLUMN IF NOT EXISTS `need_fingerprint` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否需要指纹识别：0-否 1-是' AFTER `need_face_recognition`,
ADD COLUMN IF NOT EXISTS `work_start_time` VARCHAR(10) NULL DEFAULT NULL COMMENT '工作时间开始：HH:mm' AFTER `need_fingerprint`,
ADD COLUMN IF NOT EXISTS `work_end_time` VARCHAR(10) NULL DEFAULT NULL COMMENT '工作时间结束：HH:mm' AFTER `work_start_time`,
ADD COLUMN IF NOT EXISTS `always_open` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否7x24小时开放：0-否 1-是' AFTER `work_end_time`,
ADD COLUMN IF NOT EXISTS `max_capacity` INT NULL DEFAULT NULL COMMENT '最大容纳人数' AFTER `always_open`,
ADD COLUMN IF NOT EXISTS `current_count` INT NOT NULL DEFAULT 0 COMMENT '当前人数' AFTER `max_capacity`,
ADD INDEX IF NOT EXISTS `idx_access_level` (`access_level`, `status`),
ADD INDEX IF NOT EXISTS `idx_area_capacity` (`max_capacity`, `current_count`, `status`);

-- 用户表（增强版本，用于区域关联）
ALTER TABLE `t_employee`
ADD COLUMN IF NOT EXISTS `default_access_level` TINYINT NOT NULL DEFAULT 1 COMMENT '默认访问权限级别' AFTER `disabled_flag`,
ADD COLUMN IF NOT EXISTS `card_number` VARCHAR(50) NULL DEFAULT NULL COMMENT '卡号' AFTER `default_access_level`,
ADD COLUMN IF NOT EXISTS `fingerprint_template` TEXT NULL DEFAULT NULL COMMENT '指纹模板数据' AFTER `card_number`,
ADD COLUMN IF NOT EXISTS `face_features` TEXT NULL DEFAULT NULL COMMENT '人脸特征数据' AFTER `fingerprint_template`,
ADD INDEX IF NOT EXISTS `idx_card_number` (`card_number`, `deleted_flag`),
ADD INDEX IF NOT EXISTS `idx_default_access_level` (`default_access_level`, `status`, `deleted_flag`);

-- =====================================================
-- 示例数据插入
-- =====================================================

-- 插入区域人员关联示例数据
INSERT INTO `t_area_user_relation` (
    `relation_id`, `area_id`, `area_code`, `user_id`, `username`, `real_name`,
    `relation_type`, `permission_level`, `relation_status`, `permanent`,
    `inherit_children`, `inherit_parent`, `access_permissions`, `device_sync_status`,
    `create_time`, `update_time`
) VALUES
-- 管理员在园区的权限
('area_user_001', 1, 'PARK_001', 1, 'admin', '系统管理员', 5, 4, 1, 1, 1, 1,
 '{"access": true, "attendance": true, "consume": true, "visitor": true}', 2, NOW(), NOW()),

-- 普通员工在办公区的权限
('area_user_002', 2, 'OFFICE_001', 2, 'employee001', '张三', 1, 2, 1, 1, 0, 1,
 '{"access": true, "attendance": true, "consume": true, "visitor": false}', 2, NOW(), NOW()),

-- 访客在接待区的临时权限
('area_user_003', 3, 'RECEPTION_001', 3, 'visitor001', '李四', 3, 1, 1, 0, 0, 0,
 '{"access": true, "attendance": false, "consume": false, "visitor": false}', 0, NOW(), NOW()),

-- 维护人员在设备机房的权限
('area_user_004', 4, 'SERVER_ROOM_001', 4, 'maintain001', '王五', 4, 3, 1, 1, 1, 1,
 '{"access": true, "attendance": false, "consume": false, "visitor": false}', 2, NOW(), NOW());

-- =====================================================
-- 权限配置示例
-- =====================================================

-- 门禁权限配置示例
UPDATE `t_area_user_relation`
SET `access_permissions` = JSON_OBJECT(
    'access_control', JSON_OBJECT(
        'card_access', true,
        'face_recognition', true,
        'fingerprint', false,
        'time_restriction', JSON_OBJECT(
            'start_time', '08:00',
            'end_time', '18:00',
            'workdays_only', true
        )
    ),
    'attendance', JSON_OBJECT(
        'check_in_required', true,
        'check_out_required', true,
        'break_time_required', false
    ),
    'consume', JSON_OBJECT(
        'allow_consume', true,
        'daily_limit', 100.00,
        'monthly_limit', 2000.00
    ),
    'visitor', JSON_OBJECT(
        'can_sponsor_visitor', false,
        'max_visitors_per_day', 0
    )
)
WHERE `relation_id` = 'area_user_002';

-- =====================================================
-- 触发器：区域人员变更时自动触发设备同步
-- =====================================================

DELIMITER //

-- 创建触发器：区域人员关联插入时触发设备同步
CREATE TRIGGER IF NOT EXISTS `tr_area_user_insert_sync`
AFTER INSERT ON `t_area_user_relation`
FOR EACH ROW
BEGIN
    -- 插入设备同步任务到消息队列
    -- 这里应该调用消息队列服务，暂时用日志记录
    INSERT INTO `t_operation_log` (`log_type`, `business_module`, `operation_type`, `operation_content`, `user_id`, `create_time`)
    VALUES ('DEVICE_SYNC', 'AREA_USER', 'USER_ADD',
            CONCAT('用户', NEW.user_id, '关联到区域', NEW.area_id, '，需要同步到设备'),
            NEW.create_user_id, NOW());
END//

-- 创建触发器：区域人员关联删除时触发设备权限撤销
CREATE TRIGGER IF NOT EXISTS `tr_area_user_delete_revoke`
AFTER UPDATE ON `t_area_user_relation`
FOR EACH ROW
BEGIN
    IF OLD.deleted_flag = 0 AND NEW.deleted_flag = 1 THEN
        -- 撤销设备权限
        INSERT INTO `t_operation_log` (`log_type`, `business_module`, `operation_type`, `operation_content`, `user_id`, `create_time`)
        VALUES ('DEVICE_SYNC', 'AREA_USER', 'USER_REMOVE',
                CONCAT('用户', NEW.user_id, '从区域', NEW.area_id, '移除，需要撤销设备权限'),
                NEW.update_user_id, NOW());
    END IF;
END//

DELIMITER ;

-- =====================================================
-- 存储过程：区域用户统计
-- =====================================================

DELIMITER //

-- 统计区域用户数量
CREATE PROCEDURE IF NOT EXISTS `sp_count_area_users`(
    IN p_area_id BIGINT,
    OUT p_total_users INT,
    OUT p_active_users INT,
    OUT p_permanent_users INT
)
BEGIN
    -- 总用户数
    SELECT COUNT(*) INTO p_total_users
    FROM `t_area_user_relation`
    WHERE `area_id` = p_area_id AND `deleted_flag` = 0;

    -- 有效用户数
    SELECT COUNT(*) INTO p_active_users
    FROM `t_area_user_relation`
    WHERE `area_id` = p_area_id
      AND `relation_status` = 1
      AND (`permanent` = 1 OR (`permanent` = 0 AND `expire_time` > NOW()))
      AND `deleted_flag` = 0;

    -- 永久用户数
    SELECT COUNT(*) INTO p_permanent_users
    FROM `t_area_user_relation`
    WHERE `area_id` = p_area_id
      AND `permanent` = 1
      AND `relation_status` = 1
      AND `deleted_flag` = 0;
END//

DELIMITER ;

-- =====================================================
-- 视图：区域用户权限概览
-- =====================================================

CREATE OR REPLACE VIEW `v_area_user_permissions` AS
SELECT
    aur.relation_id,
    aur.area_id,
    a.area_name,
    aur.user_id,
    e.employee_name AS user_name,
    aur.relation_type,
    aur.permission_level,
    aur.relation_status,
    aur.permanent,
    aur.device_sync_status,
    aur.access_permissions,
    aur.create_time,
    aur.expire_time
FROM `t_area_user_relation` aur
LEFT JOIN `t_common_area` a ON aur.area_id = a.area_id
LEFT JOIN `t_employee` e ON aur.user_id = e.employee_id
WHERE aur.deleted_flag = 0
  AND a.deleted_flag = 0
  AND e.deleted_flag = 0;

-- =====================================================
-- 说明文档
-- =====================================================

/*
区域-人员-设备联动架构说明：

1. 核心概念：
   - 区域作为空间概念，是物理空间的抽象
   - 人员关联到区域，获得在区域内的权限
   - 设备部署在区域内，可以识别区域内的人员
   - 人员信息可以自动下发到区域内的设备

2. 业务流程：
   - 用户管理：将人员添加到区域，设置权限级别
   - 权限继承：支持父子区域权限继承
   - 设备同步：人员信息自动下发到区域设备
   - 权限验证：设备基于区域-人员关联验证用户权限

3. 关键特性：
   - 灵活的权限控制（读/写/管理/超级管理员）
   - 权限继承机制（父子区域）
   - 时间权限控制（时间段、工作日）
   - 设备信息同步（自动下发）
   - 权限撤销机制（及时撤销）

4. 使用场景：
   - 门禁系统：人员刷卡/人脸识别验证
   - 考勤系统：区域范围内考勤记录
   - 访客管理：临时区域权限授权
   - 设备控制：基于区域权限的设备操作

5. 扩展性：
   - 支持多层区域结构（园区-楼栋-楼层-房间）
   - 支持多种人员类型（常驻/临时/访客/维护）
   - 支持多种权限验证方式（刷卡/人脸/指纹）
   - 支持灵活的权限配置（JSON格式）
*/